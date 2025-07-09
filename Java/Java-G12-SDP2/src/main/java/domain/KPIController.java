package domain;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import repository.GenericDao;
import repository.GenericDaoJpa;

public class KPIController
{

	private final GenericDao<KPI> kpiDAO;

	public KPIController()
	{
		this.kpiDAO = new GenericDaoJpa<>(KPI.class);
	}

	protected GenericDao<KPI> getKPIDao()
	{
		return kpiDAO;
	}

	public List<KPI> getAllKPIs()
	{
		List<Integer> gewensteVolgorde = List.of(3, 1, 4, 10, 12, 13);

		return kpiDAO.findAll().stream().filter(kpi -> gewensteVolgorde.contains(kpi.getId()))
				.sorted(Comparator.comparingInt(kpi -> gewensteVolgorde.indexOf(kpi.getId())))
				.collect(Collectors.toUnmodifiableList());
	}

}
