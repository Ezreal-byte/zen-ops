package com.ops.zen.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ScheduleLogExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andPkScheduleLogIsNull() {
            addCriterion("pk_schedule_log is null");
            return (Criteria) this;
        }

        public Criteria andPkScheduleLogIsNotNull() {
            addCriterion("pk_schedule_log is not null");
            return (Criteria) this;
        }

        public Criteria andPkScheduleLogEqualTo(Long value) {
            addCriterion("pk_schedule_log =", value, "pkScheduleLog");
            return (Criteria) this;
        }

        public Criteria andPkScheduleLogNotEqualTo(Long value) {
            addCriterion("pk_schedule_log <>", value, "pkScheduleLog");
            return (Criteria) this;
        }

        public Criteria andPkScheduleLogGreaterThan(Long value) {
            addCriterion("pk_schedule_log >", value, "pkScheduleLog");
            return (Criteria) this;
        }

        public Criteria andPkScheduleLogGreaterThanOrEqualTo(Long value) {
            addCriterion("pk_schedule_log >=", value, "pkScheduleLog");
            return (Criteria) this;
        }

        public Criteria andPkScheduleLogLessThan(Long value) {
            addCriterion("pk_schedule_log <", value, "pkScheduleLog");
            return (Criteria) this;
        }

        public Criteria andPkScheduleLogLessThanOrEqualTo(Long value) {
            addCriterion("pk_schedule_log <=", value, "pkScheduleLog");
            return (Criteria) this;
        }

        public Criteria andPkScheduleLogIn(List<Long> values) {
            addCriterion("pk_schedule_log in", values, "pkScheduleLog");
            return (Criteria) this;
        }

        public Criteria andPkScheduleLogNotIn(List<Long> values) {
            addCriterion("pk_schedule_log not in", values, "pkScheduleLog");
            return (Criteria) this;
        }

        public Criteria andPkScheduleLogBetween(Long value1, Long value2) {
            addCriterion("pk_schedule_log between", value1, value2, "pkScheduleLog");
            return (Criteria) this;
        }

        public Criteria andPkScheduleLogNotBetween(Long value1, Long value2) {
            addCriterion("pk_schedule_log not between", value1, value2, "pkScheduleLog");
            return (Criteria) this;
        }

        public Criteria andPkScheduleIsNull() {
            addCriterion("pk_schedule is null");
            return (Criteria) this;
        }

        public Criteria andPkScheduleIsNotNull() {
            addCriterion("pk_schedule is not null");
            return (Criteria) this;
        }

        public Criteria andPkScheduleEqualTo(Long value) {
            addCriterion("pk_schedule =", value, "pkSchedule");
            return (Criteria) this;
        }

        public Criteria andPkScheduleNotEqualTo(Long value) {
            addCriterion("pk_schedule <>", value, "pkSchedule");
            return (Criteria) this;
        }

        public Criteria andPkScheduleGreaterThan(Long value) {
            addCriterion("pk_schedule >", value, "pkSchedule");
            return (Criteria) this;
        }

        public Criteria andPkScheduleGreaterThanOrEqualTo(Long value) {
            addCriterion("pk_schedule >=", value, "pkSchedule");
            return (Criteria) this;
        }

        public Criteria andPkScheduleLessThan(Long value) {
            addCriterion("pk_schedule <", value, "pkSchedule");
            return (Criteria) this;
        }

        public Criteria andPkScheduleLessThanOrEqualTo(Long value) {
            addCriterion("pk_schedule <=", value, "pkSchedule");
            return (Criteria) this;
        }

        public Criteria andPkScheduleIn(List<Long> values) {
            addCriterion("pk_schedule in", values, "pkSchedule");
            return (Criteria) this;
        }

        public Criteria andPkScheduleNotIn(List<Long> values) {
            addCriterion("pk_schedule not in", values, "pkSchedule");
            return (Criteria) this;
        }

        public Criteria andPkScheduleBetween(Long value1, Long value2) {
            addCriterion("pk_schedule between", value1, value2, "pkSchedule");
            return (Criteria) this;
        }

        public Criteria andPkScheduleNotBetween(Long value1, Long value2) {
            addCriterion("pk_schedule not between", value1, value2, "pkSchedule");
            return (Criteria) this;
        }

        public Criteria andDtRunIsNull() {
            addCriterion("dt_run is null");
            return (Criteria) this;
        }

        public Criteria andDtRunIsNotNull() {
            addCriterion("dt_run is not null");
            return (Criteria) this;
        }

        public Criteria andDtRunEqualTo(LocalDateTime value) {
            addCriterion("dt_run =", value, "dtRun");
            return (Criteria) this;
        }

        public Criteria andDtRunNotEqualTo(LocalDateTime value) {
            addCriterion("dt_run <>", value, "dtRun");
            return (Criteria) this;
        }

        public Criteria andDtRunGreaterThan(LocalDateTime value) {
            addCriterion("dt_run >", value, "dtRun");
            return (Criteria) this;
        }

        public Criteria andDtRunGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("dt_run >=", value, "dtRun");
            return (Criteria) this;
        }

        public Criteria andDtRunLessThan(LocalDateTime value) {
            addCriterion("dt_run <", value, "dtRun");
            return (Criteria) this;
        }

        public Criteria andDtRunLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("dt_run <=", value, "dtRun");
            return (Criteria) this;
        }

        public Criteria andDtRunIn(List<LocalDateTime> values) {
            addCriterion("dt_run in", values, "dtRun");
            return (Criteria) this;
        }

        public Criteria andDtRunNotIn(List<LocalDateTime> values) {
            addCriterion("dt_run not in", values, "dtRun");
            return (Criteria) this;
        }

        public Criteria andDtRunBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("dt_run between", value1, value2, "dtRun");
            return (Criteria) this;
        }

        public Criteria andDtRunNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("dt_run not between", value1, value2, "dtRun");
            return (Criteria) this;
        }

        public Criteria andStatusRunIsNull() {
            addCriterion("status_run is null");
            return (Criteria) this;
        }

        public Criteria andStatusRunIsNotNull() {
            addCriterion("status_run is not null");
            return (Criteria) this;
        }

        public Criteria andStatusRunEqualTo(Byte value) {
            addCriterion("status_run =", value, "statusRun");
            return (Criteria) this;
        }

        public Criteria andStatusRunNotEqualTo(Byte value) {
            addCriterion("status_run <>", value, "statusRun");
            return (Criteria) this;
        }

        public Criteria andStatusRunGreaterThan(Byte value) {
            addCriterion("status_run >", value, "statusRun");
            return (Criteria) this;
        }

        public Criteria andStatusRunGreaterThanOrEqualTo(Byte value) {
            addCriterion("status_run >=", value, "statusRun");
            return (Criteria) this;
        }

        public Criteria andStatusRunLessThan(Byte value) {
            addCriterion("status_run <", value, "statusRun");
            return (Criteria) this;
        }

        public Criteria andStatusRunLessThanOrEqualTo(Byte value) {
            addCriterion("status_run <=", value, "statusRun");
            return (Criteria) this;
        }

        public Criteria andStatusRunIn(List<Byte> values) {
            addCriterion("status_run in", values, "statusRun");
            return (Criteria) this;
        }

        public Criteria andStatusRunNotIn(List<Byte> values) {
            addCriterion("status_run not in", values, "statusRun");
            return (Criteria) this;
        }

        public Criteria andStatusRunBetween(Byte value1, Byte value2) {
            addCriterion("status_run between", value1, value2, "statusRun");
            return (Criteria) this;
        }

        public Criteria andStatusRunNotBetween(Byte value1, Byte value2) {
            addCriterion("status_run not between", value1, value2, "statusRun");
            return (Criteria) this;
        }

        public Criteria andResultRunIsNull() {
            addCriterion("result_run is null");
            return (Criteria) this;
        }

        public Criteria andResultRunIsNotNull() {
            addCriterion("result_run is not null");
            return (Criteria) this;
        }

        public Criteria andResultRunEqualTo(String value) {
            addCriterion("result_run =", value, "resultRun");
            return (Criteria) this;
        }

        public Criteria andResultRunNotEqualTo(String value) {
            addCriterion("result_run <>", value, "resultRun");
            return (Criteria) this;
        }

        public Criteria andResultRunGreaterThan(String value) {
            addCriterion("result_run >", value, "resultRun");
            return (Criteria) this;
        }

        public Criteria andResultRunGreaterThanOrEqualTo(String value) {
            addCriterion("result_run >=", value, "resultRun");
            return (Criteria) this;
        }

        public Criteria andResultRunLessThan(String value) {
            addCriterion("result_run <", value, "resultRun");
            return (Criteria) this;
        }

        public Criteria andResultRunLessThanOrEqualTo(String value) {
            addCriterion("result_run <=", value, "resultRun");
            return (Criteria) this;
        }

        public Criteria andResultRunLike(String value) {
            addCriterion("result_run like", value, "resultRun");
            return (Criteria) this;
        }

        public Criteria andResultRunNotLike(String value) {
            addCriterion("result_run not like", value, "resultRun");
            return (Criteria) this;
        }

        public Criteria andResultRunIn(List<String> values) {
            addCriterion("result_run in", values, "resultRun");
            return (Criteria) this;
        }

        public Criteria andResultRunNotIn(List<String> values) {
            addCriterion("result_run not in", values, "resultRun");
            return (Criteria) this;
        }

        public Criteria andResultRunBetween(String value1, String value2) {
            addCriterion("result_run between", value1, value2, "resultRun");
            return (Criteria) this;
        }

        public Criteria andResultRunNotBetween(String value1, String value2) {
            addCriterion("result_run not between", value1, value2, "resultRun");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeIsNull() {
            addCriterion("elapsed_time is null");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeIsNotNull() {
            addCriterion("elapsed_time is not null");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeEqualTo(String value) {
            addCriterion("elapsed_time =", value, "elapsedTime");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeNotEqualTo(String value) {
            addCriterion("elapsed_time <>", value, "elapsedTime");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeGreaterThan(String value) {
            addCriterion("elapsed_time >", value, "elapsedTime");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeGreaterThanOrEqualTo(String value) {
            addCriterion("elapsed_time >=", value, "elapsedTime");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeLessThan(String value) {
            addCriterion("elapsed_time <", value, "elapsedTime");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeLessThanOrEqualTo(String value) {
            addCriterion("elapsed_time <=", value, "elapsedTime");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeLike(String value) {
            addCriterion("elapsed_time like", value, "elapsedTime");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeNotLike(String value) {
            addCriterion("elapsed_time not like", value, "elapsedTime");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeIn(List<String> values) {
            addCriterion("elapsed_time in", values, "elapsedTime");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeNotIn(List<String> values) {
            addCriterion("elapsed_time not in", values, "elapsedTime");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeBetween(String value1, String value2) {
            addCriterion("elapsed_time between", value1, value2, "elapsedTime");
            return (Criteria) this;
        }

        public Criteria andElapsedTimeNotBetween(String value1, String value2) {
            addCriterion("elapsed_time not between", value1, value2, "elapsedTime");
            return (Criteria) this;
        }

        public Criteria andRunNodeIsNull() {
            addCriterion("run_node is null");
            return (Criteria) this;
        }

        public Criteria andRunNodeIsNotNull() {
            addCriterion("run_node is not null");
            return (Criteria) this;
        }

        public Criteria andRunNodeEqualTo(String value) {
            addCriterion("run_node =", value, "runNode");
            return (Criteria) this;
        }

        public Criteria andRunNodeNotEqualTo(String value) {
            addCriterion("run_node <>", value, "runNode");
            return (Criteria) this;
        }

        public Criteria andRunNodeGreaterThan(String value) {
            addCriterion("run_node >", value, "runNode");
            return (Criteria) this;
        }

        public Criteria andRunNodeGreaterThanOrEqualTo(String value) {
            addCriterion("run_node >=", value, "runNode");
            return (Criteria) this;
        }

        public Criteria andRunNodeLessThan(String value) {
            addCriterion("run_node <", value, "runNode");
            return (Criteria) this;
        }

        public Criteria andRunNodeLessThanOrEqualTo(String value) {
            addCriterion("run_node <=", value, "runNode");
            return (Criteria) this;
        }

        public Criteria andRunNodeLike(String value) {
            addCriterion("run_node like", value, "runNode");
            return (Criteria) this;
        }

        public Criteria andRunNodeNotLike(String value) {
            addCriterion("run_node not like", value, "runNode");
            return (Criteria) this;
        }

        public Criteria andRunNodeIn(List<String> values) {
            addCriterion("run_node in", values, "runNode");
            return (Criteria) this;
        }

        public Criteria andRunNodeNotIn(List<String> values) {
            addCriterion("run_node not in", values, "runNode");
            return (Criteria) this;
        }

        public Criteria andRunNodeBetween(String value1, String value2) {
            addCriterion("run_node between", value1, value2, "runNode");
            return (Criteria) this;
        }

        public Criteria andRunNodeNotBetween(String value1, String value2) {
            addCriterion("run_node not between", value1, value2, "runNode");
            return (Criteria) this;
        }

        public Criteria andIsManualIsNull() {
            addCriterion("is_manual is null");
            return (Criteria) this;
        }

        public Criteria andIsManualIsNotNull() {
            addCriterion("is_manual is not null");
            return (Criteria) this;
        }

        public Criteria andIsManualEqualTo(Byte value) {
            addCriterion("is_manual =", value, "isManual");
            return (Criteria) this;
        }

        public Criteria andIsManualNotEqualTo(Byte value) {
            addCriterion("is_manual <>", value, "isManual");
            return (Criteria) this;
        }

        public Criteria andIsManualGreaterThan(Byte value) {
            addCriterion("is_manual >", value, "isManual");
            return (Criteria) this;
        }

        public Criteria andIsManualGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_manual >=", value, "isManual");
            return (Criteria) this;
        }

        public Criteria andIsManualLessThan(Byte value) {
            addCriterion("is_manual <", value, "isManual");
            return (Criteria) this;
        }

        public Criteria andIsManualLessThanOrEqualTo(Byte value) {
            addCriterion("is_manual <=", value, "isManual");
            return (Criteria) this;
        }

        public Criteria andIsManualIn(List<Byte> values) {
            addCriterion("is_manual in", values, "isManual");
            return (Criteria) this;
        }

        public Criteria andIsManualNotIn(List<Byte> values) {
            addCriterion("is_manual not in", values, "isManual");
            return (Criteria) this;
        }

        public Criteria andIsManualBetween(Byte value1, Byte value2) {
            addCriterion("is_manual between", value1, value2, "isManual");
            return (Criteria) this;
        }

        public Criteria andIsManualNotBetween(Byte value1, Byte value2) {
            addCriterion("is_manual not between", value1, value2, "isManual");
            return (Criteria) this;
        }

        public Criteria andPkCreatedbyIsNull() {
            addCriterion("pk_createdby is null");
            return (Criteria) this;
        }

        public Criteria andPkCreatedbyIsNotNull() {
            addCriterion("pk_createdby is not null");
            return (Criteria) this;
        }

        public Criteria andPkCreatedbyEqualTo(Long value) {
            addCriterion("pk_createdby =", value, "pkCreatedby");
            return (Criteria) this;
        }

        public Criteria andPkCreatedbyNotEqualTo(Long value) {
            addCriterion("pk_createdby <>", value, "pkCreatedby");
            return (Criteria) this;
        }

        public Criteria andPkCreatedbyGreaterThan(Long value) {
            addCriterion("pk_createdby >", value, "pkCreatedby");
            return (Criteria) this;
        }

        public Criteria andPkCreatedbyGreaterThanOrEqualTo(Long value) {
            addCriterion("pk_createdby >=", value, "pkCreatedby");
            return (Criteria) this;
        }

        public Criteria andPkCreatedbyLessThan(Long value) {
            addCriterion("pk_createdby <", value, "pkCreatedby");
            return (Criteria) this;
        }

        public Criteria andPkCreatedbyLessThanOrEqualTo(Long value) {
            addCriterion("pk_createdby <=", value, "pkCreatedby");
            return (Criteria) this;
        }

        public Criteria andPkCreatedbyIn(List<Long> values) {
            addCriterion("pk_createdby in", values, "pkCreatedby");
            return (Criteria) this;
        }

        public Criteria andPkCreatedbyNotIn(List<Long> values) {
            addCriterion("pk_createdby not in", values, "pkCreatedby");
            return (Criteria) this;
        }

        public Criteria andPkCreatedbyBetween(Long value1, Long value2) {
            addCriterion("pk_createdby between", value1, value2, "pkCreatedby");
            return (Criteria) this;
        }

        public Criteria andPkCreatedbyNotBetween(Long value1, Long value2) {
            addCriterion("pk_createdby not between", value1, value2, "pkCreatedby");
            return (Criteria) this;
        }

        public Criteria andDtCreatedIsNull() {
            addCriterion("dt_created is null");
            return (Criteria) this;
        }

        public Criteria andDtCreatedIsNotNull() {
            addCriterion("dt_created is not null");
            return (Criteria) this;
        }

        public Criteria andDtCreatedEqualTo(LocalDateTime value) {
            addCriterion("dt_created =", value, "dtCreated");
            return (Criteria) this;
        }

        public Criteria andDtCreatedNotEqualTo(LocalDateTime value) {
            addCriterion("dt_created <>", value, "dtCreated");
            return (Criteria) this;
        }

        public Criteria andDtCreatedGreaterThan(LocalDateTime value) {
            addCriterion("dt_created >", value, "dtCreated");
            return (Criteria) this;
        }

        public Criteria andDtCreatedGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("dt_created >=", value, "dtCreated");
            return (Criteria) this;
        }

        public Criteria andDtCreatedLessThan(LocalDateTime value) {
            addCriterion("dt_created <", value, "dtCreated");
            return (Criteria) this;
        }

        public Criteria andDtCreatedLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("dt_created <=", value, "dtCreated");
            return (Criteria) this;
        }

        public Criteria andDtCreatedIn(List<LocalDateTime> values) {
            addCriterion("dt_created in", values, "dtCreated");
            return (Criteria) this;
        }

        public Criteria andDtCreatedNotIn(List<LocalDateTime> values) {
            addCriterion("dt_created not in", values, "dtCreated");
            return (Criteria) this;
        }

        public Criteria andDtCreatedBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("dt_created between", value1, value2, "dtCreated");
            return (Criteria) this;
        }

        public Criteria andDtCreatedNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("dt_created not between", value1, value2, "dtCreated");
            return (Criteria) this;
        }

        public Criteria andPkModifiedbyIsNull() {
            addCriterion("pk_modifiedby is null");
            return (Criteria) this;
        }

        public Criteria andPkModifiedbyIsNotNull() {
            addCriterion("pk_modifiedby is not null");
            return (Criteria) this;
        }

        public Criteria andPkModifiedbyEqualTo(Long value) {
            addCriterion("pk_modifiedby =", value, "pkModifiedby");
            return (Criteria) this;
        }

        public Criteria andPkModifiedbyNotEqualTo(Long value) {
            addCriterion("pk_modifiedby <>", value, "pkModifiedby");
            return (Criteria) this;
        }

        public Criteria andPkModifiedbyGreaterThan(Long value) {
            addCriterion("pk_modifiedby >", value, "pkModifiedby");
            return (Criteria) this;
        }

        public Criteria andPkModifiedbyGreaterThanOrEqualTo(Long value) {
            addCriterion("pk_modifiedby >=", value, "pkModifiedby");
            return (Criteria) this;
        }

        public Criteria andPkModifiedbyLessThan(Long value) {
            addCriterion("pk_modifiedby <", value, "pkModifiedby");
            return (Criteria) this;
        }

        public Criteria andPkModifiedbyLessThanOrEqualTo(Long value) {
            addCriterion("pk_modifiedby <=", value, "pkModifiedby");
            return (Criteria) this;
        }

        public Criteria andPkModifiedbyIn(List<Long> values) {
            addCriterion("pk_modifiedby in", values, "pkModifiedby");
            return (Criteria) this;
        }

        public Criteria andPkModifiedbyNotIn(List<Long> values) {
            addCriterion("pk_modifiedby not in", values, "pkModifiedby");
            return (Criteria) this;
        }

        public Criteria andPkModifiedbyBetween(Long value1, Long value2) {
            addCriterion("pk_modifiedby between", value1, value2, "pkModifiedby");
            return (Criteria) this;
        }

        public Criteria andPkModifiedbyNotBetween(Long value1, Long value2) {
            addCriterion("pk_modifiedby not between", value1, value2, "pkModifiedby");
            return (Criteria) this;
        }

        public Criteria andDtModifiedIsNull() {
            addCriterion("dt_modified is null");
            return (Criteria) this;
        }

        public Criteria andDtModifiedIsNotNull() {
            addCriterion("dt_modified is not null");
            return (Criteria) this;
        }

        public Criteria andDtModifiedEqualTo(LocalDateTime value) {
            addCriterion("dt_modified =", value, "dtModified");
            return (Criteria) this;
        }

        public Criteria andDtModifiedNotEqualTo(LocalDateTime value) {
            addCriterion("dt_modified <>", value, "dtModified");
            return (Criteria) this;
        }

        public Criteria andDtModifiedGreaterThan(LocalDateTime value) {
            addCriterion("dt_modified >", value, "dtModified");
            return (Criteria) this;
        }

        public Criteria andDtModifiedGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("dt_modified >=", value, "dtModified");
            return (Criteria) this;
        }

        public Criteria andDtModifiedLessThan(LocalDateTime value) {
            addCriterion("dt_modified <", value, "dtModified");
            return (Criteria) this;
        }

        public Criteria andDtModifiedLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("dt_modified <=", value, "dtModified");
            return (Criteria) this;
        }

        public Criteria andDtModifiedIn(List<LocalDateTime> values) {
            addCriterion("dt_modified in", values, "dtModified");
            return (Criteria) this;
        }

        public Criteria andDtModifiedNotIn(List<LocalDateTime> values) {
            addCriterion("dt_modified not in", values, "dtModified");
            return (Criteria) this;
        }

        public Criteria andDtModifiedBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("dt_modified between", value1, value2, "dtModified");
            return (Criteria) this;
        }

        public Criteria andDtModifiedNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("dt_modified not between", value1, value2, "dtModified");
            return (Criteria) this;
        }

        public Criteria andDsIsNull() {
            addCriterion("ds is null");
            return (Criteria) this;
        }

        public Criteria andDsIsNotNull() {
            addCriterion("ds is not null");
            return (Criteria) this;
        }

        public Criteria andDsEqualTo(Byte value) {
            addCriterion("ds =", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsNotEqualTo(Byte value) {
            addCriterion("ds <>", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsGreaterThan(Byte value) {
            addCriterion("ds >", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsGreaterThanOrEqualTo(Byte value) {
            addCriterion("ds >=", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsLessThan(Byte value) {
            addCriterion("ds <", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsLessThanOrEqualTo(Byte value) {
            addCriterion("ds <=", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsIn(List<Byte> values) {
            addCriterion("ds in", values, "ds");
            return (Criteria) this;
        }

        public Criteria andDsNotIn(List<Byte> values) {
            addCriterion("ds not in", values, "ds");
            return (Criteria) this;
        }

        public Criteria andDsBetween(Byte value1, Byte value2) {
            addCriterion("ds between", value1, value2, "ds");
            return (Criteria) this;
        }

        public Criteria andDsNotBetween(Byte value1, Byte value2) {
            addCriterion("ds not between", value1, value2, "ds");
            return (Criteria) this;
        }

        public Criteria andVersionIsNull() {
            addCriterion("version is null");
            return (Criteria) this;
        }

        public Criteria andVersionIsNotNull() {
            addCriterion("version is not null");
            return (Criteria) this;
        }

        public Criteria andVersionEqualTo(Double value) {
            addCriterion("version =", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualTo(Double value) {
            addCriterion("version <>", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThan(Double value) {
            addCriterion("version >", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(Double value) {
            addCriterion("version >=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThan(Double value) {
            addCriterion("version <", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualTo(Double value) {
            addCriterion("version <=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionIn(List<Double> values) {
            addCriterion("version in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotIn(List<Double> values) {
            addCriterion("version not in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionBetween(Double value1, Double value2) {
            addCriterion("version between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotBetween(Double value1, Double value2) {
            addCriterion("version not between", value1, value2, "version");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}
