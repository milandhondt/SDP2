package domain;

import java.util.List;
import java.util.stream.Collectors;

import repository.GenericDao;
import repository.GenericDaoJpa;

public class KPIWaardeController
{

	private final GenericDao<KPIWaarde> kpiWaardeDAO;

	public KPIWaardeController()
	{
		this.kpiWaardeDAO = new GenericDaoJpa<>(KPIWaarde.class);
	}

	protected GenericDao<KPIWaarde> getKPIWaardeController()
	{
		return kpiWaardeDAO;
	}

	public List<KPIWaarde> getWaardenByKPI(int id)
	{
		return kpiWaardeDAO.findAll().stream().filter((kpiWaarde) -> kpiWaarde.getKpi().getId() == id)
				.collect(Collectors.toList());
	}

}
