package jp.co.canon.rss.logmanager.dto.analysis;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResEquipmentDTO {
    public String chamber_log_type;
    public String equipment_name;
    public String equipment_type;
    public boolean exec;
    public String fab_name;
    public String inner_tool_id;
    public String last_exec_time;
    public String log_header_type;
    public String old_log_type;
    public String phase;
    public String release;
    public String tool_id;
    public String tool_serial;
    public String user_name;
}
