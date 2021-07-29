package jp.co.canon.rss.logmanager.vo;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Entity
@TypeDef(name = "string-array", typeClass = StringArrayType.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@Table(name = "notification", schema = "log_manager")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class NotificationVo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Integer id;
	@Column(name = "job_id", nullable = false)
	private int jobId;
	@Column(name = "error_summary_enable", nullable = false)
	private Boolean errorSummaryEnable;
	@Column(name = "cras_enable", nullable = false)
	private Boolean crasEnable;
	@Column(name = "version_enable", nullable = false)
	private Boolean versionEnable;
	@Type(type = "string-array")
	@Column(name = "sending_time", columnDefinition = "text []")
	private String[] sendingTime;

//    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER, mappedBy = "notification", orphanRemoval = true)
//    private RemoteJobVo remoteJobVo;

	@OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "notification_error_fk", unique = true)
	private MailContextVo mailContextVoError;
	@OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "notification_cras_fk", unique = true)
	private MailContextVo mailContextVoCras;
	@OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "notification_version_fk", unique = true)
	private MailContextVo mailContextVoVersion;
}
