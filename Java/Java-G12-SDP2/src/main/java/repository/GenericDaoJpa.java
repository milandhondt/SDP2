package repository;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class GenericDaoJpa<T> implements GenericDao<T>
{

	private static final String PU_NAME = "shopfloor-app";
	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory(PU_NAME);
	public static final EntityManager em = emf.createEntityManager();
	private final Class<T> type;

	public GenericDaoJpa(Class<T> type)
	{
		this.type = type;
	}

	@Override
	public void closePersistency()
	{
		em.close();
		emf.close();
	}

	@Override
	public void startTransaction()
	{
		em.getTransaction().begin();
	}

	@Override
	public void commitTransaction()
	{
		em.getTransaction().commit();
	}

	@Override
	public void rollbackTransaction()
	{
		if (em.getTransaction().isActive())
		{
			em.getTransaction().rollback();
		}
	}

	@Override
	public List<T> findAll()
	{
		// return em.createNamedQuery(type.getName()+".findAll", type).getResultList();
		return em.createQuery("select entity from " + type.getSimpleName() + " entity", type).getResultList();
	}

	@Override
	public <U> T get(U id)
	{
		T entity = em.find(type, id);
		return entity;
	}

	@Override
	public T update(T object)
	{
		return em.merge(object);
	}

	@Override
	public void delete(T object)
	{
		em.remove(em.merge(object));
	}

	@Override
	public void insert(T object)
	{
		em.persist(object);
	}

	@Override
	public <U> boolean exists(U id)
	{
		T entity = em.find(type, id);
		return entity != null;
	}

}
