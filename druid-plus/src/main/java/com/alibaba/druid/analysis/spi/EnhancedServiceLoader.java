package com.alibaba.druid.analysis.spi;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnhancedServiceLoader {

    private static final ConcurrentMap<Class<?>, InnerServiceLoader<?>> SERVICE_LOADERS = new ConcurrentHashMap<>();

    public static <S> S load(Class<S> service, String activateName, ClassLoader loader) {
        return InnerServiceLoader.getServiceLoader(service).load(activateName, loader);
    }

    public static <S> S load(Class<S> service, String activateName) {
        return InnerServiceLoader.getServiceLoader(service).load(activateName, findClassLoader());
    }

    public static <S> S load(Class<S> service, String activateName, Object[] args) throws ServiceNotFoundException {
        return InnerServiceLoader.getServiceLoader(service).load(activateName, args, findClassLoader());
    }

    public static <S> S load(Class<S> service, String activateName, Class<?>[] argsType, Object[] args)
            throws ServiceNotFoundException {
        return InnerServiceLoader.getServiceLoader(service).load(activateName, argsType, args, findClassLoader());
    }

    private static ClassLoader findClassLoader() {
        return EnhancedServiceLoader.class.getClassLoader();
    }

    private static class InnerServiceLoader<S> {

        private static final Logger LOGGER = LoggerFactory.getLogger(InnerServiceLoader.class);

        private final Class<S> type;
        private final Holder<List<ExtensionDefinition<S>>> definitionsHolder = new Holder<>();
        private final ConcurrentMap<Class<?>, ExtensionDefinition<S>> classToDefinitionMap = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, List<ExtensionDefinition<S>>> nameToDefinitionsMap = new ConcurrentHashMap<>();
        private final ConcurrentMap<ExtensionDefinition<S>, Holder<Object>> definitionToInstanceMap = new ConcurrentHashMap<>();

        private static final String SERVICES_DIRECTORY = "META-INF/services/";

        private InnerServiceLoader(Class<S> type) {
            this.type = type;
        }

        @SuppressWarnings("unchecked")
        private static <S> InnerServiceLoader<S> getServiceLoader(Class<S> type) {
            if (type == null) {
                throw new IllegalArgumentException("class type is null!");
            }
            return (InnerServiceLoader<S>) computeIfAbsent(SERVICE_LOADERS, type, key -> new InnerServiceLoader<>(type));
        }

        private S load(String activateName, ClassLoader loader) {
            return loadExtension(activateName, loader, null, null);
        }

        private S load(String activateName, Object[] args, ClassLoader loader) throws ServiceNotFoundException {
            Class<?>[] argsType = null;
            if (args != null && args.length > 0) {
                argsType = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    argsType[i] = args[i].getClass();
                }
            }
            return loadExtension(activateName, loader, argsType, args);
        }

        private S load(String activateName, Class<?>[] argsType, Object[] args, ClassLoader loader)
                throws ServiceNotFoundException {
            return loadExtension(activateName, loader, argsType, args);
        }

        private S loadExtension(String activeName, ClassLoader loader, Class[] argTypes, Object[] args) {
            if (StringUtils.isBlank(activeName)) {
                throw new IllegalArgumentException("activeName must not be null!");
            }
            try {
                loadAllExtensionClass(loader);
                ExtensionDefinition<S> extensionDefinition = getCachedExtensionDefinition(activeName);
                return getExtensionInstance(extensionDefinition, loader, argTypes, args);
            } catch (Exception e) {
                throw new ServiceNotFoundException("not found service provider for : " + type.getName());
            }
        }

        private S getExtensionInstance(ExtensionDefinition<S> definition, ClassLoader loader, Class<?>[] argTypes,
                                       Object[] args) {
            if (definition == null) {
                throw new ServiceNotFoundException("not found service provider for : " + type.getName());
            }
            if (Scope.SINGLETON == definition.getScope()) {
                Holder<Object> holder = computeIfAbsent(definitionToInstanceMap, definition, key -> new Holder<>());
                Object instance = holder.get();
                if (instance == null) {
                    synchronized (holder) {
                        instance = holder.get();
                        if (instance == null) {
                            instance = createNewExtension(definition, loader, argTypes, args);
                            holder.set(instance);
                        }
                    }
                }
                return (S) instance;
            } else {
                return createNewExtension(definition, loader, argTypes, args);
            }
        }

        private S createNewExtension(ExtensionDefinition<S> definition, ClassLoader loader, Class<?>[] argTypes, Object[] args) {
            Class<S> clazz = definition.getServiceClass();
            try {
                return initInstance(clazz, argTypes, args);
            } catch (Throwable t) {
                throw new IllegalStateException("Extension instance(definition: " + definition + ", class: " +
                        type + ")  could not be instantiated: " + t.getMessage(), t);
            }
        }

        private S initInstance(Class<S> implClazz, Class<?>[] argTypes, Object[] args)
                throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
            S s = null;
            if (argTypes != null && args != null) {
                Constructor<S> constructor = implClazz.getDeclaredConstructor(argTypes);
                s = type.cast(constructor.newInstance(args));
            } else {
                s = type.cast(implClazz.newInstance());
            }
            if (s instanceof Initialize) {
                ((Initialize) s).init();
            }
            return s;
        }

        private List<Class<S>> loadAllExtensionClass(ClassLoader loader) {
            List<ExtensionDefinition<S>> definitions = definitionsHolder.get();
            if (definitions == null) {
                synchronized (definitionsHolder) {
                    definitions = definitionsHolder.get();
                    if (definitions == null) {
                        definitions = findAllExtensionDefinition(loader);
                        definitionsHolder.set(definitions);
                    }
                }
            }
            return definitions.stream().map(ExtensionDefinition::getServiceClass).collect(Collectors.toList());
        }

        private List<ExtensionDefinition<S>> findAllExtensionDefinition(ClassLoader loader) {
            List<ExtensionDefinition<S>> extensionDefinitions = new ArrayList<>();
            try {
                loadFile(SERVICES_DIRECTORY, loader, extensionDefinitions);
            } catch (IOException e) {
                throw new ServiceNotFoundException(e);
            }
            return extensionDefinitions;
        }

        private void loadFile(String dir, ClassLoader loader, List<ExtensionDefinition<S>> extensions) throws IOException {
            String fileName = dir + type.getName();
            Enumeration<URL> urls;
            if (loader != null) {
                urls = loader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }

            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            final int ci = line.indexOf('#');
                            if (ci > 0) {
                                line = line.substring(0, ci);
                            }
                            line = line.trim();
                            if (line.length() > 0) {
                                try {
                                    ExtensionDefinition<S> extensionDefinition = getUnloadedExtensionDefinition(line, loader);
                                    if (extensionDefinition == null) {
                                        continue;
                                    }
                                    extensions.add(extensionDefinition);
                                } catch (LinkageError | ClassNotFoundException e) {
                                    LOGGER.warn("Load [{}] class fail. {}", line, e.getMessage());
                                } catch (ClassCastException e) {
                                    LOGGER.error("Load [{}] class fail, please make sure the extension config in {} implements {}.", line, fileName, type.getName());
                                }
                            }
                        }
                    } catch (Throwable e) {
                        LOGGER.warn("load class instance error: {}", e.getMessage());
                    }
                }
            }
        }

        private ExtensionDefinition<S> getUnloadedExtensionDefinition(String className, ClassLoader loader) throws ClassNotFoundException, ClassCastException {
            if (!isDefinitionContainsClazz(className, loader)) {
                Class<?> clazz = Class.forName(className, true, loader);
                if (!type.isAssignableFrom(clazz)) {
                    throw new ClassCastException("class类型一致性错误");
                }
                Class<S> enhancedServiceClass = (Class<S>) clazz;
                String serviceName = null;
                int priority = 0;
                Scope scope = Scope.SINGLETON;
                Service service = enhancedServiceClass.getAnnotation(Service.class);
                if (service != null) {
                    serviceName = service.name();
                    scope = service.scope();
                    priority = service.order();
                }
                ExtensionDefinition<S> result =
                        new ExtensionDefinition<>(serviceName, priority, scope, enhancedServiceClass);
                classToDefinitionMap.put(clazz, result);
                if (serviceName != null) {
                    computeIfAbsent(nameToDefinitionsMap, serviceName, e -> new ArrayList<>())
                            .add(result);
                }
                return result;
            }
            return null;
        }

        private boolean isDefinitionContainsClazz(String className, ClassLoader loader) {
            for (Map.Entry<Class<?>, ExtensionDefinition<S>> entry : classToDefinitionMap.entrySet()) {
                if (!entry.getKey().getName().equals(className)) {
                    continue;
                }
                if (Objects.equals(entry.getValue().getServiceClass().getClassLoader(), loader)) {
                    return true;
                }
            }
            return false;
        }

        private ExtensionDefinition<S> getCachedExtensionDefinition(String activateName) {
            List<ExtensionDefinition<S>> definitions = nameToDefinitionsMap.get(activateName);
            if (CollectionUtils.isEmpty(definitions)) {
                return null;
            }
            int size;
            while (true) {
                size = definitions.size();
                if (size == 0) {
                    return null;
                }
                try {
                    return definitions.get(size - 1);
                } catch (IndexOutOfBoundsException ex) {
                }
            }
        }


        /**
         * Helper Class for hold a value.
         *
         * @param <T>
         */
        private static class Holder<T> {
            private volatile T value;

            private void set(T value) {
                this.value = value;
            }

            private T get() {
                return value;
            }
        }

    }

    public static <K, V> V computeIfAbsent(Map<K, V> map, K key, Function<? super K, ? extends V> mappingFunction) {
        V value = map.get(key);
        if (value != null) {
            return value;
        }
        return map.computeIfAbsent(key, mappingFunction);
    }
}
