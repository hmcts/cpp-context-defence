package uk.gov.moj.defence.helper;

import uk.gov.justice.services.test.utils.common.host.TestHostProvider;

import org.apache.commons.dbcp2.BasicDataSource;

public class PostgresDataSourceProvider {
    private static final BasicDataSource dataSource;

    static {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUsername("defence");
        dataSource.setPassword("defence");
        dataSource.setUrl("jdbc:postgresql://" + TestHostProvider.getHost() + ":5432/defenceviewstore");

    }

    public static BasicDataSource getPostgresDataSource() {
        return dataSource;
    }
}
