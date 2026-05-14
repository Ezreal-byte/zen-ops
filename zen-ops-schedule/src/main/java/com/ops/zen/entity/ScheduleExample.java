package com.ops.zen.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ScheduleExample() {
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

        public Criteria andPkScheduleGroupIsNull() {
            addCriterion("pk_schedule_group is null");
            return (Criteria) this;
        }

        public Criteria andPkScheduleGroupIsNotNull() {
            addCriterion("pk_schedule_group is not null");
            return (Criteria) this;
        }

        public Criteria andPkScheduleGroupEqualTo(Long value) {
            addCriterion("pk_schedule_group =", value, "pkScheduleGroup");
            return (Criteria) this;
        }

        public Criteria andPkScheduleGroupNotEqualTo(Long value) {
            addCriterion("pk_schedule_group <>", value, "pkScheduleGroup");
            return (Criteria) this;
        }

        public Criteria andPkScheduleGroupGreaterThan(Long value) {
            addCriterion("pk_schedule_group >", value, "pkScheduleGroup");
            return (Criteria) this;
        }

        public Criteria andPkScheduleGroupGreaterThanOrEqualTo(Long value) {
            addCriterion("pk_schedule_group >=", value, "pkScheduleGroup");
            return (Criteria) this;
        }

        public Criteria andPkScheduleGroupLessThan(Long value) {
            addCriterion("pk_schedule_group <", value, "pkScheduleGroup");
            return (Criteria) this;
        }

        public Criteria andPkScheduleGroupLessThanOrEqualTo(Long value) {
            addCriterion("pk_schedule_group <=", value, "pkScheduleGroup");
            return (Criteria) this;
        }

        public Criteria andPkScheduleGroupIn(List<Long> values) {
            addCriterion("pk_schedule_group in", values, "pkScheduleGroup");
            return (Criteria) this;
        }

        public Criteria andPkScheduleGroupNotIn(List<Long> values) {
            addCriterion("pk_schedule_group not in", values, "pkScheduleGroup");
            return (Criteria) this;
        }

        public Criteria andPkScheduleGroupBetween(Long value1, Long value2) {
            addCriterion("pk_schedule_group between", value1, value2, "pkScheduleGroup");
            return (Criteria) this;
        }

        public Criteria andPkScheduleGroupNotBetween(Long value1, Long value2) {
            addCriterion("pk_schedule_group not between", value1, value2, "pkScheduleGroup");
            return (Criteria) this;
        }

        public Criteria andNameIsNull() {
            addCriterion("name is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("name is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("name =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("name <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("name >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("name >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("name <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("name <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("name like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("name not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("name in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("name not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("name between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("name not between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andJobClassIsNull() {
            addCriterion("job_class is null");
            return (Criteria) this;
        }

        public Criteria andJobClassIsNotNull() {
            addCriterion("job_class is not null");
            return (Criteria) this;
        }

        public Criteria andJobClassEqualTo(String value) {
            addCriterion("job_class =", value, "jobClass");
            return (Criteria) this;
        }

        public Criteria andJobClassNotEqualTo(String value) {
            addCriterion("job_class <>", value, "jobClass");
            return (Criteria) this;
        }

        public Criteria andJobClassGreaterThan(String value) {
            addCriterion("job_class >", value, "jobClass");
            return (Criteria) this;
        }

        public Criteria andJobClassGreaterThanOrEqualTo(String value) {
            addCriterion("job_class >=", value, "jobClass");
            return (Criteria) this;
        }

        public Criteria andJobClassLessThan(String value) {
            addCriterion("job_class <", value, "jobClass");
            return (Criteria) this;
        }

        public Criteria andJobClassLessThanOrEqualTo(String value) {
            addCriterion("job_class <=", value, "jobClass");
            return (Criteria) this;
        }

        public Criteria andJobClassLike(String value) {
            addCriterion("job_class like", value, "jobClass");
            return (Criteria) this;
        }

        public Criteria andJobClassNotLike(String value) {
            addCriterion("job_class not like", value, "jobClass");
            return (Criteria) this;
        }

        public Criteria andJobClassIn(List<String> values) {
            addCriterion("job_class in", values, "jobClass");
            return (Criteria) this;
        }

        public Criteria andJobClassNotIn(List<String> values) {
            addCriterion("job_class not in", values, "jobClass");
            return (Criteria) this;
        }

        public Criteria andJobClassBetween(String value1, String value2) {
            addCriterion("job_class between", value1, value2, "jobClass");
            return (Criteria) this;
        }

        public Criteria andJobClassNotBetween(String value1, String value2) {
            addCriterion("job_class not between", value1, value2, "jobClass");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeIsNull() {
            addCriterion("trigger_type is null");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeIsNotNull() {
            addCriterion("trigger_type is not null");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeEqualTo(String value) {
            addCriterion("trigger_type =", value, "triggerType");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeNotEqualTo(String value) {
            addCriterion("trigger_type <>", value, "triggerType");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeGreaterThan(String value) {
            addCriterion("trigger_type >", value, "triggerType");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeGreaterThanOrEqualTo(String value) {
            addCriterion("trigger_type >=", value, "triggerType");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeLessThan(String value) {
            addCriterion("trigger_type <", value, "triggerType");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeLessThanOrEqualTo(String value) {
            addCriterion("trigger_type <=", value, "triggerType");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeLike(String value) {
            addCriterion("trigger_type like", value, "triggerType");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeNotLike(String value) {
            addCriterion("trigger_type not like", value, "triggerType");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeIn(List<String> values) {
            addCriterion("trigger_type in", values, "triggerType");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeNotIn(List<String> values) {
            addCriterion("trigger_type not in", values, "triggerType");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeBetween(String value1, String value2) {
            addCriterion("trigger_type between", value1, value2, "triggerType");
            return (Criteria) this;
        }

        public Criteria andTriggerTypeNotBetween(String value1, String value2) {
            addCriterion("trigger_type not between", value1, value2, "triggerType");
            return (Criteria) this;
        }

        public Criteria andTriggerExprIsNull() {
            addCriterion("trigger_expr is null");
            return (Criteria) this;
        }

        public Criteria andTriggerExprIsNotNull() {
            addCriterion("trigger_expr is not null");
            return (Criteria) this;
        }

        public Criteria andTriggerExprEqualTo(String value) {
            addCriterion("trigger_expr =", value, "triggerExpr");
            return (Criteria) this;
        }

        public Criteria andTriggerExprNotEqualTo(String value) {
            addCriterion("trigger_expr <>", value, "triggerExpr");
            return (Criteria) this;
        }

        public Criteria andTriggerExprGreaterThan(String value) {
            addCriterion("trigger_expr >", value, "triggerExpr");
            return (Criteria) this;
        }

        public Criteria andTriggerExprGreaterThanOrEqualTo(String value) {
            addCriterion("trigger_expr >=", value, "triggerExpr");
            return (Criteria) this;
        }

        public Criteria andTriggerExprLessThan(String value) {
            addCriterion("trigger_expr <", value, "triggerExpr");
            return (Criteria) this;
        }

        public Criteria andTriggerExprLessThanOrEqualTo(String value) {
            addCriterion("trigger_expr <=", value, "triggerExpr");
            return (Criteria) this;
        }

        public Criteria andTriggerExprLike(String value) {
            addCriterion("trigger_expr like", value, "triggerExpr");
            return (Criteria) this;
        }

        public Criteria andTriggerExprNotLike(String value) {
            addCriterion("trigger_expr not like", value, "triggerExpr");
            return (Criteria) this;
        }

        public Criteria andTriggerExprIn(List<String> values) {
            addCriterion("trigger_expr in", values, "triggerExpr");
            return (Criteria) this;
        }

        public Criteria andTriggerExprNotIn(List<String> values) {
            addCriterion("trigger_expr not in", values, "triggerExpr");
            return (Criteria) this;
        }

        public Criteria andTriggerExprBetween(String value1, String value2) {
            addCriterion("trigger_expr between", value1, value2, "triggerExpr");
            return (Criteria) this;
        }

        public Criteria andTriggerExprNotBetween(String value1, String value2) {
            addCriterion("trigger_expr not between", value1, value2, "triggerExpr");
            return (Criteria) this;
        }

        public Criteria andPrincipalIsNull() {
            addCriterion("principal is null");
            return (Criteria) this;
        }

        public Criteria andPrincipalIsNotNull() {
            addCriterion("principal is not null");
            return (Criteria) this;
        }

        public Criteria andPrincipalEqualTo(String value) {
            addCriterion("principal =", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalNotEqualTo(String value) {
            addCriterion("principal <>", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalGreaterThan(String value) {
            addCriterion("principal >", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalGreaterThanOrEqualTo(String value) {
            addCriterion("principal >=", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalLessThan(String value) {
            addCriterion("principal <", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalLessThanOrEqualTo(String value) {
            addCriterion("principal <=", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalLike(String value) {
            addCriterion("principal like", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalNotLike(String value) {
            addCriterion("principal not like", value, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalIn(List<String> values) {
            addCriterion("principal in", values, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalNotIn(List<String> values) {
            addCriterion("principal not in", values, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalBetween(String value1, String value2) {
            addCriterion("principal between", value1, value2, "principal");
            return (Criteria) this;
        }

        public Criteria andPrincipalNotBetween(String value1, String value2) {
            addCriterion("principal not between", value1, value2, "principal");
            return (Criteria) this;
        }

        public Criteria andIsActiveIsNull() {
            addCriterion("is_active is null");
            return (Criteria) this;
        }

        public Criteria andIsActiveIsNotNull() {
            addCriterion("is_active is not null");
            return (Criteria) this;
        }

        public Criteria andIsActiveEqualTo(Byte value) {
            addCriterion("is_active =", value, "isActive");
            return (Criteria) this;
        }

        public Criteria andIsActiveNotEqualTo(Byte value) {
            addCriterion("is_active <>", value, "isActive");
            return (Criteria) this;
        }

        public Criteria andIsActiveGreaterThan(Byte value) {
            addCriterion("is_active >", value, "isActive");
            return (Criteria) this;
        }

        public Criteria andIsActiveGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_active >=", value, "isActive");
            return (Criteria) this;
        }

        public Criteria andIsActiveLessThan(Byte value) {
            addCriterion("is_active <", value, "isActive");
            return (Criteria) this;
        }

        public Criteria andIsActiveLessThanOrEqualTo(Byte value) {
            addCriterion("is_active <=", value, "isActive");
            return (Criteria) this;
        }

        public Criteria andIsActiveIn(List<Byte> values) {
            addCriterion("is_active in", values, "isActive");
            return (Criteria) this;
        }

        public Criteria andIsActiveNotIn(List<Byte> values) {
            addCriterion("is_active not in", values, "isActive");
            return (Criteria) this;
        }

        public Criteria andIsActiveBetween(Byte value1, Byte value2) {
            addCriterion("is_active between", value1, value2, "isActive");
            return (Criteria) this;
        }

        public Criteria andIsActiveNotBetween(Byte value1, Byte value2) {
            addCriterion("is_active not between", value1, value2, "isActive");
            return (Criteria) this;
        }

        public Criteria andLastRunStatusIsNull() {
            addCriterion("last_run_status is null");
            return (Criteria) this;
        }

        public Criteria andLastRunStatusIsNotNull() {
            addCriterion("last_run_status is not null");
            return (Criteria) this;
        }

        public Criteria andLastRunStatusEqualTo(Byte value) {
            addCriterion("last_run_status =", value, "lastRunStatus");
            return (Criteria) this;
        }

        public Criteria andLastRunStatusNotEqualTo(Byte value) {
            addCriterion("last_run_status <>", value, "lastRunStatus");
            return (Criteria) this;
        }

        public Criteria andLastRunStatusGreaterThan(Byte value) {
            addCriterion("last_run_status >", value, "lastRunStatus");
            return (Criteria) this;
        }

        public Criteria andLastRunStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("last_run_status >=", value, "lastRunStatus");
            return (Criteria) this;
        }

        public Criteria andLastRunStatusLessThan(Byte value) {
            addCriterion("last_run_status <", value, "lastRunStatus");
            return (Criteria) this;
        }

        public Criteria andLastRunStatusLessThanOrEqualTo(Byte value) {
            addCriterion("last_run_status <=", value, "lastRunStatus");
            return (Criteria) this;
        }

        public Criteria andLastRunStatusIn(List<Byte> values) {
            addCriterion("last_run_status in", values, "lastRunStatus");
            return (Criteria) this;
        }

        public Criteria andLastRunStatusNotIn(List<Byte> values) {
            addCriterion("last_run_status not in", values, "lastRunStatus");
            return (Criteria) this;
        }

        public Criteria andLastRunStatusBetween(Byte value1, Byte value2) {
            addCriterion("last_run_status between", value1, value2, "lastRunStatus");
            return (Criteria) this;
        }

        public Criteria andLastRunStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("last_run_status not between", value1, value2, "lastRunStatus");
            return (Criteria) this;
        }

        public Criteria andDesIsNull() {
            addCriterion("des is null");
            return (Criteria) this;
        }

        public Criteria andDesIsNotNull() {
            addCriterion("des is not null");
            return (Criteria) this;
        }

        public Criteria andDesEqualTo(String value) {
            addCriterion("des =", value, "des");
            return (Criteria) this;
        }

        public Criteria andDesNotEqualTo(String value) {
            addCriterion("des <>", value, "des");
            return (Criteria) this;
        }

        public Criteria andDesGreaterThan(String value) {
            addCriterion("des >", value, "des");
            return (Criteria) this;
        }

        public Criteria andDesGreaterThanOrEqualTo(String value) {
            addCriterion("des >=", value, "des");
            return (Criteria) this;
        }

        public Criteria andDesLessThan(String value) {
            addCriterion("des <", value, "des");
            return (Criteria) this;
        }

        public Criteria andDesLessThanOrEqualTo(String value) {
            addCriterion("des <=", value, "des");
            return (Criteria) this;
        }

        public Criteria andDesLike(String value) {
            addCriterion("des like", value, "des");
            return (Criteria) this;
        }

        public Criteria andDesNotLike(String value) {
            addCriterion("des not like", value, "des");
            return (Criteria) this;
        }

        public Criteria andDesIn(List<String> values) {
            addCriterion("des in", values, "des");
            return (Criteria) this;
        }

        public Criteria andDesNotIn(List<String> values) {
            addCriterion("des not in", values, "des");
            return (Criteria) this;
        }

        public Criteria andDesBetween(String value1, String value2) {
            addCriterion("des between", value1, value2, "des");
            return (Criteria) this;
        }

        public Criteria andDesNotBetween(String value1, String value2) {
            addCriterion("des not between", value1, value2, "des");
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
