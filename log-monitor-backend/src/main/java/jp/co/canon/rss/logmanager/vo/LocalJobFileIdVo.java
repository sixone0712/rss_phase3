package jp.co.canon.rss.logmanager.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "localjobfileid")
@Getter
@Setter
@Accessors(chain = true)
public class LocalJobFileIdVo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "upload_date", unique = true)
    private LocalDateTime uploadDate;
    @Column(name = "file_name", unique = true, columnDefinition = "TEXT")
    private String fileName;
    @Column(name = "file_original_name", columnDefinition = "TEXT")
    private String fileOriginalName;
}
