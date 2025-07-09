package domain;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import util.Grafiek;
import util.Role;
import util.RoleListConverter;

@Entity
@Getter
@Setter
@Table(name = "kpis")
public class KPI implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String onderwerp;

	@Convert(converter = RoleListConverter.class)
	@Column(name = "roles", columnDefinition = "TEXT")
	private List<Role> roles;

	@Enumerated(EnumType.STRING)
	private Grafiek grafiek;

	@OneToMany(mappedBy = "kpi", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<KPIWaarde> kpiwaarden;
}
