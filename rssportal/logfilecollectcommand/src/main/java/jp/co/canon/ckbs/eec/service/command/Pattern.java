package jp.co.canon.ckbs.eec.service.command;

public class Pattern {
    String pattern;
    PatternType patternType = PatternType.OTHER;

    public Pattern(String pattern){

        this.pattern = pattern;
    }

    public Pattern(String pattern, PatternType patternType){
        this.pattern = pattern;
        this.patternType = patternType;
    }

    public boolean matches(String str){
        return str.matches(this.pattern);
    }

    public enum PatternType {
        YEAR,
        MONTH,
        DAY,
        OTHER
    }
}
