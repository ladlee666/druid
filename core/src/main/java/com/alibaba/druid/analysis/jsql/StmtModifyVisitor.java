package com.alibaba.druid.analysis.jsql;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.analysis.jsql.parser.ParserUtil;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.statement.upsert.Upsert;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author LENOVO
 * @date 2024/6/3 16:17
 */
public class StmtModifyVisitor implements StatementVisitor, SelectVisitor,
        FromItemVisitor, ExpressionVisitor, SelectItemVisitor, OrderByVisitor,
        GroupByVisitor {

    /**
     * 表名规则映射
     * {"USER_INFO": ["Create"]}
     */
    private Map<String, Set<String>> prefixMappings;

    /**
     * 表名修改
     */
    private Map<String, String> tableMappings;

    /**
     * 前缀
     */
    private String prefix;

    /**
     * 连接符
     */
    private final String CONNECTOR = "_";

    /**
     * 关键字
     */
//    private final KeyWord[] KEYWORDS = new KeyWord[5];

    /**
     * @param tableMappings 修改表名
     */
    public StmtModifyVisitor(Map<String, String> tableMappings) {
        this.tableMappings = tableMappings;
    }

    /**
     * 给指定表加上前缀
     *
     * @param prefixMappings
     * @param prefix
     * @param keyWords
     */
    public StmtModifyVisitor(Map<String, Set<String>> prefixMappings, String prefix, KeyWord... keyWords) {
        this.prefixMappings = prefixMappings;
        this.prefix = prefix;
//        if (keyWords != null && keyWords.length > 0) {
//            if (keyWords.length > 5) {
//                throw new ArrayStoreException("keyWords的最大长度为5");
//            }
//            System.arraycopy(keyWords, 0, KEYWORDS, 0, keyWords.length);
//        }
    }

    /**
     * 给所有表名都加上前缀
     *
     * @param prefix
     */
    public StmtModifyVisitor(String prefix) {
        this.prefix = prefix;
    }

    ////////////////////////////////////select start/////////////////////////////////
    @Override
    public void visit(ParenthesedSelect parenthesedSelect) {
        Select select = parenthesedSelect.getSelect();
        select.accept((SelectVisitor) this);

    }

    @Override
    public void visit(PlainSelect plainSelect) {
        // from
        visitFromItem(plainSelect.getFromItem());
        // join
        visitJoins(plainSelect.getJoins());
        // where
        visitExpr(plainSelect.getWhere());
        // group by
        visit(plainSelect.getGroupBy());
        // having
        visitExpr(plainSelect.getHaving());
        // select
        visitSelectItems(plainSelect.getSelectItems());
        // order by
        visitOrderBy(plainSelect.getOrderByElements());
    }

    public void visitFromItem(FromItem fromItem) {
        if (fromItem != null) {
            fromItem.accept(this);
        }
    }

    public void visitExpr(Expression expr) {
        if (expr != null) {
            expr.accept(this);
        }
    }

    public void visitOrderBy(List<OrderByElement> orderByElements) {
        if (CollectionUtils.isNotEmpty(orderByElements)) {
            for (OrderByElement orderByElement : orderByElements) {
                orderByElement.accept(this);
            }
        }
    }

    public void visitSelectItems(List<SelectItem<?>> selectItems) {
        if (CollectionUtils.isNotEmpty(selectItems)) {
            for (SelectItem<?> selectItem : selectItems) {
                selectItem.accept(this);
            }
        }
    }

    @Override
    public void visit(SetOperationList setOperationList) {
        List<Select> selects = setOperationList.getSelects();
        for (Select select : selects) {
            select.accept((SelectVisitor) this);
        }
    }

    @Override
    public void visit(WithItem withItem) {
    }

    @Override
    public void visit(Values values) {
        visit(values.getExpressions());
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {

    }

    @Override
    public void visit(TableStatement tableStatement) {

    }

    ////////////////////////////////////select end/////////////////////////////////


    ////////////////////////////////////statement start/////////////////////////////////
    @Override
    public void visit(Analyze analyze) {

    }

    @Override
    public void visit(SavepointStatement savepointStatement) {

    }

    @Override
    public void visit(RollbackStatement rollbackStatement) {

    }

    @Override
    public void visit(Comment comment) {

    }

    @Override
    public void visit(Commit commit) {

    }

    @Override
    public void visit(Delete delete) {
        List<Table> tables = delete.getTables();
        for (Table table : tables) {
            visit(table);
        }
        visitJoins(delete.getJoins());
        Expression where = delete.getWhere();
        if (where != null) {
            where.accept(this);
        }
    }

    public void visit(Join join) {
        FromItem joinFromItem = join.getFromItem();
        joinFromItem.accept(this);
        Collection<Expression> expressions = join.getOnExpressions();
        for (Expression expression : expressions) {
            expression.accept(this);
        }
    }

    public void visitJoins(List<Join> joins) {
        if (CollectionUtils.isNotEmpty(joins)) {
            for (Join join : joins) {
                visit(join);
            }
        }
    }

    @Override
    public void visit(Select select) {
        select.accept((SelectVisitor) this);
    }

    @Override
    public void visit(Update update) {
        visit(update.getTable());
        List<UpdateSet> updateSets = update.getUpdateSets();
        for (UpdateSet updateSet : updateSets) {
            ExpressionList<Column> columns = updateSet.getColumns();
            if (CollectionUtils.isNotEmpty(columns)) {
                visit(columns);
            }
        }
        FromItem fromItem = update.getFromItem();
        if (fromItem != null) {
            fromItem.accept(this);
        }
        Expression where = update.getWhere();
        if (where != null) {
            where.accept(this);
        }
        List<Join> joins = update.getJoins();
        visitJoins(joins);
    }

    @Override
    public void visit(Insert insert) {
        visit(insert.getTable());
        visit(insert.getColumns());
        Values values = insert.getValues();
        if (values != null) {
            values.accept((SelectVisitor) this);
        }
    }

    @Override
    public void visit(Drop drop) {
        Table table = drop.getName();
        visit(table);
    }

    @Override
    public void visit(Truncate truncate) {

    }

    @Override
    public void visit(CreateIndex createIndex) {

    }

    @Override
    public void visit(CreateSchema createSchema) {

    }

    @Override
    public void visit(CreateTable createTable) {

    }

    @Override
    public void visit(CreateView createView) {

    }

    @Override
    public void visit(AlterView alterView) {

    }

    @Override
    public void visit(RefreshMaterializedViewStatement refreshMaterializedViewStatement) {

    }

    @Override
    public void visit(Alter alter) {

    }

    @Override
    public void visit(Statements statements) {
    }

    @Override
    public void visit(Execute execute) {

    }

    @Override
    public void visit(SetStatement setStatement) {

    }

    @Override
    public void visit(ResetStatement resetStatement) {

    }

    @Override
    public void visit(ShowColumnsStatement showColumnsStatement) {

    }

    @Override
    public void visit(ShowIndexStatement showIndexStatement) {

    }

    @Override
    public void visit(ShowTablesStatement showTablesStatement) {

    }

    @Override
    public void visit(Merge merge) {

    }

    @Override
    public void visit(Upsert upsert) {

    }

    @Override
    public void visit(UseStatement useStatement) {

    }

    @Override
    public void visit(Block block) {

    }

    @Override
    public void visit(DescribeStatement describeStatement) {

    }

    @Override
    public void visit(ExplainStatement explainStatement) {

    }

    @Override
    public void visit(ShowStatement showStatement) {

    }

    @Override
    public void visit(DeclareStatement declareStatement) {

    }

    @Override
    public void visit(Grant grant) {

    }

    @Override
    public void visit(CreateSequence createSequence) {

    }

    @Override
    public void visit(AlterSequence alterSequence) {

    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {

    }

    @Override
    public void visit(CreateSynonym createSynonym) {

    }

    @Override
    public void visit(AlterSession alterSession) {

    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {

    }

    @Override
    public void visit(RenameTableStatement renameTableStatement) {

    }

    @Override
    public void visit(PurgeStatement purgeStatement) {

    }

    @Override
    public void visit(AlterSystemStatement alterSystemStatement) {

    }

    @Override
    public void visit(UnsupportedStatement unsupportedStatement) {
        throw new UnsupportedOperationException("不支持的SQL语句!");
    }

    ////////////////////////////////////statement end/////////////////////////////////

    ///////////////////////////////////FromItemVisitor////////////////////////////////

    @Override
    public void visit(Table table) {
        if (table == null) {
            return;
        }
        String tableName = table.getName().toUpperCase();
        if (MapUtil.isNotEmpty(prefixMappings) && StrUtil.isNotBlank(prefix)) {
            if (prefixMappings.containsKey(tableName)) {
                table.setName(prefix + CONNECTOR + tableName);
                return;
            }
//            Set<String> stats = tableMappings.get(tableName);
//            if (stats != null) {
//                boolean match = stats.stream().anyMatch(s -> {
//                    for (KeyWord keyword : KEYWORDS) {
//                        if (null != keyword) {
//                            return s.equals(keyword.getWord());
//                        }
//                    }
//                    return false;
//                });
//                if (match) {
//                    table.setName(prefix + CONNECTOR + tableName);
//                }
//            }
        }
        if (StrUtil.isNotBlank(prefix) && MapUtil.isEmpty(prefixMappings)) {
            table.setName(prefix + CONNECTOR + tableName);
            return;
        }
        if (MapUtil.isNotEmpty(tableMappings) && tableMappings.containsKey(tableName)) {
            String newTableName = tableMappings.get(tableName);
            table.setName(newTableName);
        }
    }

    @Override
    public void visit(TableFunction tableFunction) {

    }

    @Override
    public void visit(ParenthesedFromItem parenthesedFromItem) {

    }

    ///////////////////////////////////FromItemVisitor////////////////////////////////

    ///////////////////////////////////ExpressionVisitor////////////////////////////////

    @Override
    public void visit(BitwiseRightShift bitwiseRightShift) {

    }

    @Override
    public void visit(BitwiseLeftShift bitwiseLeftShift) {

    }

    @Override
    public void visit(NullValue nullValue) {

    }

    @Override
    public void visit(Function function) {
        ExpressionList<?> parameters = function.getParameters();
        parameters.accept(this);
    }

    @Override
    public void visit(SignedExpression signedExpression) {

    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {

    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {

    }

    @Override
    public void visit(DoubleValue doubleValue) {

    }

    @Override
    public void visit(LongValue longValue) {

    }

    @Override
    public void visit(HexValue hexValue) {

    }

    @Override
    public void visit(DateValue dateValue) {

    }

    @Override
    public void visit(TimeValue timeValue) {

    }

    @Override
    public void visit(TimestampValue timestampValue) {

    }

    @Override
    public void visit(Parenthesis parenthesis) {
        visitExpr(parenthesis.getExpression());
    }

    @Override
    public void visit(StringValue stringValue) {

    }

    @Override
    public void visit(Addition addition) {

    }

    @Override
    public void visit(Division division) {

    }

    @Override
    public void visit(IntegerDivision integerDivision) {

    }

    @Override
    public void visit(Multiplication multiplication) {

    }

    @Override
    public void visit(Subtraction subtraction) {

    }

    @Override
    public void visit(AndExpression andExpression) {
        visitExpr(andExpression.getLeftExpression());
        visitExpr(andExpression.getRightExpression());
    }

    @Override
    public void visit(OrExpression orExpression) {
        visitExpr(orExpression.getLeftExpression());
        visitExpr(orExpression.getRightExpression());
    }

    @Override
    public void visit(XorExpression xorExpression) {
        visitExpr(xorExpression.getLeftExpression());
        visitExpr(xorExpression.getRightExpression());
    }

    @Override
    public void visit(Between between) {
        visitExpr(between.getLeftExpression());
        visitExpr(between.getBetweenExpressionStart());
        visitExpr(between.getBetweenExpressionEnd());
    }

    @Override
    public void visit(OverlapsCondition overlapsCondition) {

    }

    @Override
    public void visit(EqualsTo equalsTo) {
        visitExpr(equalsTo.getLeftExpression());
        visitExpr(equalsTo.getRightExpression());
    }

    @Override
    public void visit(GreaterThan greaterThan) {

    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {

    }

    @Override
    public void visit(InExpression inExpression) {
        visitExpr(inExpression.getLeftExpression());
        visitExpr(inExpression.getRightExpression());
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {

    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        visitExpr(isNullExpression.getLeftExpression());
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {

    }

    @Override
    public void visit(LikeExpression likeExpression) {
        visitExpr(likeExpression.getLeftExpression());
        visitExpr(likeExpression.getRightExpression());
    }

    @Override
    public void visit(MinorThan minorThan) {

    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {

    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitExpr(notEqualsTo.getLeftExpression());
        visitExpr(notEqualsTo.getRightExpression());
    }

    @Override
    public void visit(DoubleAnd doubleAnd) {

    }

    @Override
    public void visit(Contains contains) {

    }

    @Override
    public void visit(ContainedBy containedBy) {

    }

    @Override
    public void visit(CaseExpression caseExpression) {
        visitExpr(caseExpression.getSwitchExpression());
    }

    @Override
    public void visit(WhenClause whenClause) {

    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        visitExpr(existsExpression.getRightExpression());
    }

    @Override
    public void visit(MemberOfExpression memberOfExpression) {

    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {

    }

    @Override
    public void visit(Concat concat) {

    }

    @Override
    public void visit(Matches matches) {

    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {

    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {

    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {

    }

    @Override
    public void visit(CastExpression castExpression) {

    }

    @Override
    public void visit(Modulo modulo) {

    }

    @Override
    public void visit(AnalyticExpression analyticExpression) {

    }

    @Override
    public void visit(ExtractExpression extractExpression) {

    }

    @Override
    public void visit(IntervalExpression intervalExpression) {

    }

    @Override
    public void visit(OracleHierarchicalExpression oracleHierarchicalExpression) {

    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {

    }

    @Override
    public void visit(JsonExpression jsonExpression) {

    }

    @Override
    public void visit(JsonOperator jsonOperator) {

    }

    @Override
    public void visit(UserVariable userVariable) {

    }

    @Override
    public void visit(NumericBind numericBind) {

    }

    @Override
    public void visit(KeepExpression keepExpression) {

    }

    @Override
    public void visit(MySQLGroupConcat mySQLGroupConcat) {

    }

    @Override
    public void visit(ExpressionList<?> expressionList) {
        if (CollectionUtils.isNotEmpty(expressionList)) {
            if (CollectionUtils.isNotEmpty(expressionList)) {
                for (Expression expression : expressionList) {
                    expression.accept(this);
                }
            }
        }
    }

    @Override
    public void visit(RowConstructor<?> rowConstructor) {

    }

    @Override
    public void visit(RowGetExpression rowGetExpression) {

    }

    @Override
    public void visit(OracleHint oracleHint) {

    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {

    }

    @Override
    public void visit(DateTimeLiteralExpression dateTimeLiteralExpression) {

    }

    @Override
    public void visit(NotExpression notExpression) {

    }

    @Override
    public void visit(NextValExpression nextValExpression) {

    }

    @Override
    public void visit(CollateExpression collateExpression) {

    }

    @Override
    public void visit(SimilarToExpression similarToExpression) {

    }

    @Override
    public void visit(ArrayExpression arrayExpression) {

    }

    @Override
    public void visit(ArrayConstructor arrayConstructor) {

    }

    @Override
    public void visit(VariableAssignment variableAssignment) {

    }

    @Override
    public void visit(XMLSerializeExpr xmlSerializeExpr) {

    }

    @Override
    public void visit(TimezoneExpression timezoneExpression) {

    }

    @Override
    public void visit(JsonAggregateFunction jsonAggregateFunction) {

    }

    @Override
    public void visit(JsonFunction jsonFunction) {

    }

    @Override
    public void visit(ConnectByRootOperator connectByRootOperator) {

    }

    @Override
    public void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter) {

    }

    @Override
    public void visit(AllColumns allColumns) {
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
    }

    @Override
    public void visit(Column column) {

    }

    @Override
    public void visit(AllValue allValue) {

    }

    @Override
    public void visit(IsDistinctExpression isDistinctExpression) {

    }

    @Override
    public void visit(GeometryDistance geometryDistance) {

    }

    @Override
    public void visit(TranscodingFunction transcodingFunction) {

    }

    @Override
    public void visit(TrimFunction trimFunction) {

    }

    @Override
    public void visit(RangeExpression rangeExpression) {

    }

    @Override
    public void visit(TSQLLeftJoin tsqlLeftJoin) {
    }

    @Override
    public void visit(TSQLRightJoin tsqlRightJoin) {
    }
    ///////////////////////////////////ExpressionVisitor////////////////////////////////

    ///////////////////////////////////SelectItemVisitor////////////////////////////////
    @Override
    public void visit(SelectItem selectItem) {
        visitExpr(selectItem.getExpression());
    }
    ///////////////////////////////////SelectItemVisitor////////////////////////////////

    ///////////////////////////////////OrderByVisitor////////////////////////////////
    @Override
    public void visit(OrderByElement orderByElement) {
        visitExpr(orderByElement.getExpression());
    }
    ///////////////////////////////////OrderByVisitor////////////////////////////////

    ///////////////////////////////////OrderByVisitor////////////////////////////////
    @Override
    public void visit(GroupByElement groupByElement) {
        if (groupByElement != null) {
            visit(groupByElement.getGroupByExpressionList());
        }
    }
    ///////////////////////////////////OrderByVisitor////////////////////////////////
}
