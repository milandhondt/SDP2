package util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class JPAUtil
{
	@Getter
	private final static EntityManagerFactory entityManagerFactory = Persistence
			.createEntityManagerFactory("shopfloor-app");
}
