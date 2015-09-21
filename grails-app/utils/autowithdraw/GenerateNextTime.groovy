package autowithdraw
/**
 * Created by IntelliJ IDEA.
 * User: Bernie
 * Date: 12-2-6
 * Time: 下午2:59
 * To change this template use File | Settings | File Templates.
 */
class GenerateNextTime {
    static get(cycle_expr, now, incHoliday, type) {
        //type: 1 日  2 周  3 月
        def ret
        switch (type) {//['1': '按日', '2': '按周', '3': '按月']
            case 1:       //'1': '按日'
                ret = getNextTimeByDate(cycle_expr, now)
                break;
            case 2:        //'2': '按周'
                ret = getNextTimeByWeek(cycle_expr, now)
                break;
            case 3:        //'3': '按月'
                ret = getNextTimeByMonth(cycle_expr, now)
                break;
        }
        return ret
    }

    private static getNextTimeByDate(cycle_expr, Date now) {
        //按日提现，生成下次提现时间
        def expr = cycle_expr.tokenize('[,]')
        def hour = now.hours
        def set = new HashSet(expr.size());
        set.addAll(expr);
        expr = new ArrayList<Integer>(set.size());
        set.each {el ->
            expr.add(el.toInteger())
        }
        expr.add(hour)
        expr.sort()
        def idx = expr.findLastIndexOf {it == hour}
        expr.remove(idx)

        //如果是最后一个,则下次运行为第二天第一个时间点,否则为当天下一个时间点
        if (idx == (expr.size())) {
            def nextDate = now + 1
            return new Date(nextDate.year, nextDate.month, nextDate.date, expr[0], 0, 0)
        } else {
            return new Date(now.year, now.month, now.date, expr[idx], 0, 0)
        }
    }

    private static getNextTimeByWeek(cycle_expr, Date now) {
        //生成下次提现时间 - 周
        def expr = cycle_expr.tokenize('[,]')
        def set = new HashSet(expr.size());
        expr.eachWithIndex {el, idx ->
            def item = el.tokenize('#')
            set.add item[0].toInteger() * 100 + item[1].toInteger()
        }

        def cur = now.day * 100 + now.hours

        expr = set as ArrayList;
        expr.add cur
        expr.sort()
        def idx = expr.findLastIndexOf {it == cur}
        expr.remove(idx)
        //如果是最后一个,则下次运行为序列中第一个时间点,否则为序列中下一个时间点
        if (idx == (expr.size())) {   //最后一个,则下次运行为序列中第一个时间点
            def nextHour = expr[0] % 100
            def nextWeek = (expr[0] - nextHour) / 100
            def nextDate = now + (nextWeek + 7 - now.day).toInteger()
            return new Date(nextDate.year, nextDate.month, nextDate.date, nextHour, 0, 0)
        } else {
            def nextHour = expr[idx] % 100
            def nextWeek = (expr[idx] - nextHour) / 100
            def nextDate = now + (nextWeek - now.day).toInteger()
            return new Date(nextDate.year, nextDate.month, nextDate.date, nextHour, 0, 0)
        }
    }

    private static getNextTimeByMonth(cycle_expr, Date now) {
        //生成下次提现时间 - 月
        def expr = cycle_expr.tokenize('[,]')
        def set = new HashSet(expr.size());
        expr.eachWithIndex {el, idx ->
            def item = el.tokenize('#')
            set.add item[0].toInteger() * 100 + item[1].toInteger()
        }

        def cur = now.date * 100 + now.hours
        expr = set as ArrayList;
        expr.add cur
        expr.sort()
        def idx = expr.findLastIndexOf {it == cur}
        expr.remove(idx)

        //如果是最后一个,则下次运行为序列中第一个时间点,否则为序列中下一个时间点
        if (idx == (expr.size())) {
            def nextHour = expr[0] % 100
            def nextDate = (expr[0] - nextHour) / 100

            //下月天数
            def nextMonthLastDay = getNextMonthLastDay(now, 1)
            //如果大于月末，则提前到月末
            nextDate = nextDate > nextMonthLastDay.date ? nextMonthLastDay.date : nextDate

            return new Date(nextMonthLastDay.year, nextMonthLastDay.month, nextDate.toInteger(), nextHour.toInteger(), 0, 0)
        } else {
            def nextHour = expr[idx] % 100
            def nextDate = (expr[idx] - nextHour) / 100

            //本月天数
            def monthLastDay = getCurMonthLastDay(now, 1)

            //如果大于月末，则提前到月末
            if (nextDate >= monthLastDay.date) {

                if (monthLastDay.date * 100 + nextHour <= cur) {
                    //已经执行过，调整为下月第一个
                    nextHour = expr[0] % 100
                    nextDate = (expr[0] - nextHour) / 100
                    //下月天数
                    def nextMonthLastDay = getNextMonthLastDay(now, 1)
                    //如果大于月末，则提前到月末
                    nextDate = nextDate > nextMonthLastDay.date ? nextMonthLastDay.date : nextDate
                    return new Date(nextMonthLastDay.year, nextMonthLastDay.month, nextDate.toInteger(), nextHour.toInteger(), 0, 0)
                } else {
                    return new Date(monthLastDay.year, monthLastDay.month, monthLastDay.date, nextHour.toInteger(), 0, 0)
                }
            } else {
                return new Date(monthLastDay.year, monthLastDay.month, nextDate.toInteger(), nextHour.toInteger(), 0, 0)
            }
        }
    }

    private static generateNextTimeByDate(cycle_expr, Date now, incHoliday) {
        //生成下次提现时间 - 日
        def expr = cycle_expr.tokenize('[,]')
        def hour = now.hours
        def set = new HashSet(expr.size());
        set.addAll(expr);
        expr = new ArrayList<Integer>(set.size());
        set.each {el ->
            expr.add(el.toInteger())
        }
        expr.add(hour)
        expr.sort()
        def idx = expr.findLastIndexOf {it == hour}
        expr.remove(idx)
        //如果是最后一个,则下次运行为第二天第一个时间点,否则为当天下一个时间点
        if (idx == (expr.size())) {   //第二天第一个时间点
            def nextDay = getNextWorkday(now, incHoliday)

            return new Date(nextDay.year, nextDay.month, nextDay.date, expr[0], 0, 0)

        } else {      //当天下一个时间点
            if (incHoliday == 1) {   //节假日提现
                return new Date(now.year, now.month, now.date, expr[idx], 0, 0)
            } else {          //节假日不提现: 判断当天是否周末,如是周末,则为下一工作日第一个时间点
                def nextDay = new Date()
                switch (now.day) {
                    case 6:
                        nextDay = now + 2
                        nextDay = new Date(nextDay.year, nextDay.month, nextDay.date, expr[0], 0, 0)
                        break;
                    case 0:
                        nextDay = now + 1
                        nextDay = new Date(nextDay.year, nextDay.month, nextDay.date, expr[0], 0, 0)
                        break;
                    default:
                        nextDay = new Date(now.year, now.month, now.date, expr[idx], 0, 0)
                        break;
                }
                return nextDay
            }
        }

    }

    private static generateNextTimeByWeek(cycle_expr, now, incHoliday) {
        //生成下次提现时间 - 周
        def expr = cycle_expr.tokenize('[,]')
        def set = new HashSet(expr.size());
        expr.eachWithIndex {el, idx ->
            def item = el.tokenize('#')
            set.add item[0].toInteger() * 100 + item[1].toInteger()
        }

        def cur = now.day * 100 + now.hours
        //set.add cur

        expr = set as ArrayList;
        expr.add cur
        expr.sort()
        def idx = expr.findLastIndexOf {it == cur}
        expr.remove(idx)
        //如果是最后一个,则下次运行为序列中第一个时间点,否则为序列中下一个时间点
        if (idx == (expr.size())) {   //最后一个,则下次运行为序列中第一个时间点
            def nextDay = new Date()
            if (incHoliday == 1) {   //节假日提现
                //下一次运行的星期数
                def nextHour = expr[0] % 100
                def nextWeek = (expr[0] - nextHour) / 100
                nextDay = now + (nextWeek + 7 - now.day).toInteger()
                return new Date(nextDay.year, nextDay.month, nextDay.date, nextHour, 0, 0)
            } else {            //节假日不提现.暂只判断周末.如需判断完整节假日,需要添加"工作日历"功能
                if (expr[0] < 600) {
                    //周一至周五
                    def nextHour = expr[0] % 100
                    def nextWeek = (expr[0] - nextHour) / 100
                    nextDay = now + (nextWeek + 7 - now.day).toInteger()
                    return new Date(nextDay.year, nextDay.month, nextDay.date, nextHour, 0, 0)
                } else {
                    //周六\日
                    return null
                }
            }
        } else {      //序列中下一个时间点
            def nextDay = new Date()
            if (incHoliday == 1) {   //节假日提现
                def nextHour = expr[idx] % 100
                def nextWeek = (expr[idx] - nextHour) / 100
                nextDay = now + (nextWeek - now.day).toInteger()
                return new Date(nextDay.year, nextDay.month, nextDay.date, nextHour, 0, 0)
            } else {                  //节假日不提现
                if (expr[idx] >= 600) {
                    //后面全是周末 ,选取第一个
                    if (expr[0] >= 600) {
                        //第一个也是周末
                        return null
                    } else {
                        def nextHour = expr[0] % 100
                        def nextWeek = (expr[0] - nextHour) / 100
                        nextDay = now + (nextWeek + 7 - now.day).toInteger()
                        return new Date(nextDay.year, nextDay.month, nextDay.date, nextHour, 0, 0)
                    }
                } else {
                    def nextHour = expr[idx] % 100
                    def nextWeek = (expr[idx] - nextHour) / 100
                    nextDay = now + (nextWeek - now.day).toInteger()
                    return new Date(nextDay.year, nextDay.month, nextDay.date, nextHour, 0, 0)
                }
            }
        }
    }

    private static generateNextTimeByMonth(cycle_expr, now, incHoliday) {
        //生成下次提现时间 - 月
        def expr = cycle_expr.tokenize('[,]')
        def set = new HashSet(expr.size());
        expr.eachWithIndex {el, idx ->
            def item = el.tokenize('#')
            set.add item[0].toInteger() * 100 + item[1].toInteger()
        }

        def cur = now.date * 100 + now.hours
        //set.add cur

        expr = set as ArrayList;
        expr.add cur
        expr.sort()
        def idx = expr.findLastIndexOf {it == cur}
        expr.remove(idx)

        //如果是最后一个,则下次运行为序列中第一个时间点,否则为序列中下一个时间点
        if (idx == (expr.size())) {
            return getNextMonthFirst(expr[0], now, incHoliday)
        } else {

            def nextHour = expr[idx] % 100
            def nextDate = (expr[idx] - nextHour) / 100

            def nextDay = new Date(now.year, now.month, nextDate.toInteger(), nextHour.toInteger(), 0, 0)

            //如果节假日不提现且是周末，顺延
            if (incHoliday == 0 && (nextDay.day == 6 || nextDay.day == 0)) { //周末不提现，如果是周末取下一个工作日
                nextDay = getNextWorkday(nextDay, incHoliday)
            }

            def monthLastDay = getCurMonthLastDay(now, incHoliday)


            if (nextDay > monthLastDay) {
                //下一个日期如果大于月末,则提前到月末执行
                if (monthLastDay.date * 100 + nextHour <= cur) {        //monthLastDay.date == now.date   //当日已经提现，不再提现，转到下月
                    //如果月末的时点已经执行过，则调整到下月第一个时点
                    nextDay = getNextMonthFirst(expr[0], now, incHoliday)
                } else {
                    //否则为月末执行
                    nextDay = new Date(monthLastDay.year, monthLastDay.month, monthLastDay.date, nextHour, 0, 0)
                }
            } else {
                //否则顺延到下一工作日

            }

            return new Date(monthLastDay.year, monthLastDay.month, nextDate.toInteger(), nextHour, 0, 0)
        }
    }

    private static getNextMonthFirst(expr, now, incHoliday) {
        //得到下月第一个执行点
        def nextHour = expr % 100
        def nextDate = (expr - nextHour) / 100

        //得到下月最后一天
        def nextMonthLastDay = getNextMonthLastDay(now, incHoliday)

        def nextDay = new Date(nextMonthLastDay.year, nextMonthLastDay.month, nextDate.toInteger(), nextHour.toInteger(), 0, 0)

        if (incHoliday == 0 && (nextDay.day == 6 || nextDay.day == 0)) { //周末不提现，如果是周末取下一个工作日
            nextDay = getNextWorkday(nextDay, incHoliday)
        }

        nextDay = nextDay > nextMonthLastDay ? nextMonthLastDay : nextDay //下次执行日期大于月末,则提前到月末执行

        return new Date(nextDay.year, nextDay.month, nextDay.date, nextHour, 0, 0)
    }

    private static getNextWorkday(now, incHoliday) {
        def nextDay = new Date()
        if (incHoliday == 1) {   //节假日提现
            nextDay = now + 1
        } else {            //节假日不提现.暂只判断周末.如需判断完整节假日,需要添加"工作日历"功能
            switch (now.day) {
                case 5:
                    nextDay = now + 3
                    break;
                case 6:
                    nextDay = now + 2
                    break;
                default:
                    nextDay = now + 1
                    break;
            }
        }
        return nextDay
    }

    private static getCurMonthLastDay(now, incHoliday) {
        //取得当月末一天
        //param: now:当前时间, Date
        //       incHoliday: 是否包含节假日  0 不含 1 包含
        return getMonthLastDay(now, incHoliday, 0)
    }

    private static getNextMonthLastDay(now, incHoliday) {
        //取得下月末一天
        //param: now:当前时间, Date
        //       incHoliday: 是否包含节假日  0 不含 1 包含
        return getMonthLastDay(now, incHoliday, 1)
    }

    private static getMonthLastDay(now, incHoliday, type) {
        //取得月末一天
        //param: now:当前时间, Date
        //       incHoliday: 是否包含节假日  0 不含 1 包含
        //       type: 类型 0 当月 1 下月
        Calendar c = Calendar.getInstance()
        c.set(now.year, now.month + type, 1)
        def d = c.getActualMaximum(Calendar.DAY_OF_MONTH)

        def date = new Date(c.get(Calendar.YEAR), c.get(Calendar.MONTH), d, 0, 0, 0)
        if (incHoliday == 0) {
            switch (date.day) {
                case 0:  //周日,前移两天
                    d = d - 2
                    break;
                case 6:  //周六,前移一天
                    d = d - 1
                    break;
                default:
                    break;
            }
        }
        return new Date(c.get(Calendar.YEAR), c.get(Calendar.MONTH), d, 0, 0, 0)
    }
}
