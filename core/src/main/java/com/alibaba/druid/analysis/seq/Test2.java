package com.alibaba.druid.analysis.seq;//package com.alibaba.druid.analysis.seq;
//
//import com.alibaba.druid.analysis.seq.builder.MongoSeqBuilder;
//import com.alibaba.druid.analysis.seq.sequence.Sequence;
//import com.mongodb.ConnectionString;
//import com.mongodb.MongoClientSettings;
//import com.mongodb.MongoDriverInformation;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import org.springframework.data.mongodb.core.MongoTemplate;
//
///**
// * @author LENOVO
// * @date 2024/5/29 15:38
// */
//public class Test2 {
//
//    public static void main(String[] args) {
//        MongoTemplate template = get("mongodb://10.10.31.26:27017", "lowcode");
//        Sequence sequence = MongoSeqBuilder.create().bizName(() -> "test").mongoTemplate(template).build();
//        for (int i = 0; i < 5; i++) {
//            String no = sequence.nextNo("%03d", "AB");
//            System.out.println(no);
//        }
//
//    }
//
//    public static MongoTemplate get(String uri, String database) {
//        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(uri)).build();
//        MongoDriverInformation driverInformation = MongoDriverInformation.builder().build();
//        MongoClient client = MongoClients.create(settings, driverInformation);
//        return new MongoTemplate(client, database);
//    }
//}
