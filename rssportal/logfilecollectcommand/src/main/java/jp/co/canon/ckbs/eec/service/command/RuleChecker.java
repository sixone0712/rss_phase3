package jp.co.canon.ckbs.eec.service.command;

import java.util.ArrayList;
import java.util.List;

public class RuleChecker {
    Pattern[] patterns = new Pattern[0];
    int count = 0;
    RuleCheckerDate startDate = null;
    RuleCheckerDate endDate = null;

    private RuleChecker(){

    }

    public void setPeriod(String startDateStr, String endDateStr){
        if (startDateStr != null) {
            this.startDate = dateStrToRuleCheckerDate(startDateStr);
        }
        if (endDateStr != null) {
            this.endDate = dateStrToRuleCheckerDate(endDateStr);
        }
    }

    public static RuleCheckerDate dateStrToRuleCheckerDate(String dateStr){
        RuleCheckerDate date = new RuleCheckerDate();
        date.setYear(Integer.parseInt(dateStr.substring(0, 4)));
        date.setMonth(Integer.parseInt(dateStr.substring(4, 6)));
        date.setDay(Integer.parseInt(dateStr.substring(6, 8)));
        return date;
    }

    static boolean checkYear(RuleCheckerDate current, RuleCheckerDate start, RuleCheckerDate end){
        if (start != null){
            if (current.getYear() < start.getYear()){
                return false;
            }
        }
        if (end != null){
            if (current.getYear() > end.getYear()){
                return false;
            }
        }
        return true;
    }

    static boolean checkMonth(RuleCheckerDate current, RuleCheckerDate start, RuleCheckerDate end){
        if (!checkYear(current, start, end)){
            return false;
        }
        if (start != null){
            if (current.getYear() == start.getYear()){
                if (current.getMonth() < start.getMonth()){
                    return false;
                }
            }
        }
        if (end != null){
            if (current.getYear() == end.getMonth()){
                if (current.getMonth() > end.getMonth()){
                    return false;
                }
            }
        }
        return true;
    }

    static boolean checkDay(RuleCheckerDate current, RuleCheckerDate start, RuleCheckerDate end){
        if (!checkYear(current, start, end)){
            return false;
        }
        if (!checkMonth(current, start, end)){
            return false;
        }
        if (start != null){
            if (current.getYear() == start.getYear()){
                if (current.getMonth() == start.getMonth()){
                    if (current.getDay() < start.getDay()){
                        return false;
                    }
                }
            }
        }
        if (end != null){
            if (current.getYear() == end.getYear()){
                if (current.getMonth() == end.getMonth()){
                    if (current.getDay() > end.getDay()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    void applyPattern(String patternStr){
        List<Pattern> patternArray = new ArrayList<>();
        String[] patternArr = patternStr.split("/");

        count = 0;
        for(String patStr : patternArr){
            if (patStr.length() > 0) {
                if (patStr.equals("YYMMDD")){
                    patternArray.add(new Pattern("^20[0-9][0-9]", Pattern.PatternType.YEAR));
                    patternArray.add(new Pattern("^[01][0-9]", Pattern.PatternType.MONTH));
                    patternArray.add(new Pattern("^[0-3][0-9]", Pattern.PatternType.DAY));
                } else if (patStr.equals("yyyymmddhhmmss") || patStr.equals("YYYYMMDDhhmmss")){
                    Pattern pattern = new Pattern("^20[0-9][0-9][01][0-9][0-3][0-9].+");
                    patternArray.add(pattern);
                } else {
                    String convertedPatternStr = patStr.replace(".", "\\.")
                            .replace("*", ".+");
                    Pattern pattern = new Pattern(convertedPatternStr);
                    patternArray.add(pattern);
                }
                count++;
            }
        }
        patterns = patternArray.toArray(new Pattern[0]);
    }

    public int getCount(){
        return count;
    }

    public boolean matches(String currentDirPath, String str, int index){
        if (index >= count){
            return false;
        }
        if (patterns[index].patternType == Pattern.PatternType.OTHER){
            return patterns[index].matches(str);
        }
        if (patterns[index].patternType == Pattern.PatternType.YEAR){
            if (!patterns[index].matches(str)){
                return false;
            }
            RuleCheckerDate checkerDate = new RuleCheckerDate();
            checkerDate.setYear(Integer.parseInt(str));
            return checkYear(checkerDate, startDate, endDate);
        }
        if (patterns[index].patternType == Pattern.PatternType.MONTH){
            if (!patterns[index].matches(str)){
                return false;
            }
            String[] dirArr = currentDirPath.split("/");
            int year = Integer.parseInt(dirArr[dirArr.length - 1]);
            int month = Integer.parseInt(str);
            if (month < 1 || month > 12){
                return false;
            }
            RuleCheckerDate checkerDate = new RuleCheckerDate();
            checkerDate.setYear(year);
            checkerDate.setMonth(month);
            return checkMonth(checkerDate, startDate, endDate);
        }
        if (patterns[index].patternType == Pattern.PatternType.DAY){
            if (!patterns[index].matches(str)){
                return false;
            }
            String[] dirArr = currentDirPath.split("/");
            int year = Integer.parseInt(dirArr[dirArr.length - 2]);
            int month = Integer.parseInt(dirArr[dirArr.length - 1]);
            int day = Integer.parseInt(str);
            if (day < 1 || day > 31){
                return false;
            }
            RuleCheckerDate checkerDate = new RuleCheckerDate();
            checkerDate.setYear(year);
            checkerDate.setMonth(month);
            checkerDate.setDay(day);
            return checkDay(checkerDate, startDate, endDate);
        }
        return false;
    }

    public static RuleChecker create(String patternStr){
        RuleChecker ruleChecker = new RuleChecker();

        ruleChecker.applyPattern(patternStr);

        return ruleChecker;
    }
}
