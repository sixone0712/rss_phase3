package jp.co.canon.rss.logmanager.vo;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@TypeDef(name = "int-array", typeClass = IntArrayType.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@Table(name = "remotejob", schema = "log_manager")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class RemoteJobVo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Integer jobId;
	@Column(name = "site_id")
	private int siteId;
	@Column(name = "collect_status", nullable = false)
	private String collectStatus;
	@Column(name = "error_summary_status", nullable = false)
	private String errorSummaryStatus;
	@Column(name = "cras_status", nullable = false)
	private String crasDataStatus;
	@Column(name = "version_check_status", nullable = false)
	private String mpaVersionStatus;
	@Column(name = "stop", nullable = false)
	private boolean stop;
	@Column(name = "owner", nullable = false)
	private int owner;
	@Column(name = "created", nullable = false)
	private LocalDateTime created;
	@Column(name = "last_action")
	private LocalDateTime lastAction;
	@Type(type = "int-array")
	@Column(name = "plan_id", columnDefinition = "integer []")
	private int[] planIds;

	@OneToOne
	@JoinColumn(name = "site_vo_list_fk", unique = true)
	private SiteVo siteVoList;

	@OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "notification_id_fk", unique = true)
	private NotificationVo notification;
}
