package domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "kpiwaarden", uniqueConstraints = { @UniqueConstraint(columnNames = { "kpi_id", "datum", "site_id" }) })
public class KPIWaarde
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private LocalDateTime datum;

	@Column(columnDefinition = "json")
	private String waarde;

	private String site_id;

	@ManyToOne
	@JoinColumn(name = "kpi_id")
	private KPI kpi;

	@Override
	public String toString()
	{
		return waarde;
	}

}
