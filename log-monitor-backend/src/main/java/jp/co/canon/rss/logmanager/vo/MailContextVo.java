package jp.co.canon.rss.logmanager.vo;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import jp.co.canon.rss.logmanager.vo.address.JobAddressBookEntity;
import jp.co.canon.rss.logmanager.vo.address.JobGroupBookEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@TypeDef(name = "string-array", typeClass = StringArrayType.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@Table(name = "mail_context", schema = "log_manager")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class MailContextVo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Integer id;
	@Type(type = "string-array")
	@Column(name = "custom_emails", columnDefinition = "text []")
	private String[] customEmails;
	@Column(name = "subject", columnDefinition = "TEXT")
	private String subject;
	@Column(name = "body", columnDefinition = "TEXT")
	private String body;
	@Column(name = "before")
	private int before;

//    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER, mappedBy="mailContextVoError", orphanRemoval = true)
//    private NotificationVo notificationVoErrorList;
//    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER, mappedBy="mailContextVoCras", orphanRemoval = true)
//    private NotificationVo notificationVoCrasList;
//    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER, mappedBy="mailContextVoVersion", orphanRemoval = true)
//    private NotificationVo notificationVoVersionList;

	@OneToMany(mappedBy = "mailContext", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<JobAddressBookEntity> address = new HashSet<>();

	@OneToMany(mappedBy = "mailContext", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	private Set<JobGroupBookEntity> group = new HashSet<>();
}
