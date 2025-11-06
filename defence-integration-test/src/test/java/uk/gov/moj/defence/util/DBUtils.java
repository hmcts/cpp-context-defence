package uk.gov.moj.defence.util;

import static uk.gov.moj.defence.helper.PostgresDataSourceProvider.getPostgresDataSource;

import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBUtils.class);

    private static final String SELECT_DEFENCE_CLIENT_COLUMNS_FOR_MAPPING_TO_DEFENCECLIENT_ENTITY =
            "SELECT id, first_name AS firstName, last_name AS lastName, date_of_birth, case_id AS caseId, is_visible AS isVisible, organisation_name organisationName";

    private static final String SELECT_DEFENCE_CLIENT_BY_FIRSTNAME_LASTNAME_DOB_QUERY =
            SELECT_DEFENCE_CLIENT_COLUMNS_FOR_MAPPING_TO_DEFENCECLIENT_ENTITY + " from defence_client where first_name = ? and last_name = ? and date_of_birth = ?";

    private static final String SELECT_DEFENCE_CLIENT_BY_DEFENCE_CLIENT_ID_QUERY =
            SELECT_DEFENCE_CLIENT_COLUMNS_FOR_MAPPING_TO_DEFENCECLIENT_ENTITY + " from defence_client where id = ?";


    public static Optional<DefenceClient> selectDefenceClient(final String firstName, final String lastName, final LocalDate dob) {

        final QueryRunner queryRunner = new QueryRunner(getPostgresDataSource());
        final ResultSetHandler<DefenceClient> resultSetHandler = new BeanHandler<>(DefenceClient.class);

        try {
            return Optional.ofNullable(queryRunner.query(SELECT_DEFENCE_CLIENT_BY_FIRSTNAME_LASTNAME_DOB_QUERY, resultSetHandler, firstName, lastName, dob));
        } catch (SQLException e) {
            LOGGER.debug(e.getMessage());
            return Optional.empty();
        }
    }

    public static Optional<DefenceClient> selectDefenceClient(final UUID defenceClientId) {

        final QueryRunner queryRunner = new QueryRunner(getPostgresDataSource());
        final ResultSetHandler<DefenceClient> resultSetHandler = new BeanHandler<>(DefenceClient.class);

        try {
            return Optional.ofNullable(queryRunner.query(SELECT_DEFENCE_CLIENT_BY_DEFENCE_CLIENT_ID_QUERY, resultSetHandler, defenceClientId));
        } catch (SQLException e) {
            LOGGER.debug(e.getMessage());
            return Optional.empty();
        }
    }

    public static void insertProsecutionAdvocateAccessRecords(final String currentDate, final String pastDate) throws IOException, SQLException {
        final QueryRunner queryRunner = new QueryRunner(getPostgresDataSource());
        final File inserSqlFile = new File(DBUtils.class.getClassLoader().getResource("insert_prosecution_advocate_access.sql").getFile());
        final List<String> lines = FileUtils.readLines(inserSqlFile, StandardCharsets.UTF_8);

        // clean up old data
        queryRunner.update("delete from assignment_user_details where user_id in ('038d0500-3436-4320-bc68-b465534931c8', '02f551c8-e516-4f7e-b650-8fe997259d8e')");
        queryRunner.update("delete from prosecution_advocate_access where case_id in ('b996b3cc-8613-4281-bdeb-bc480628e4d9', '5bc84b03-0fdc-49bd-bb47-b365ade0bb42', 'c0ea3f7f-ec71-4520-8581-2d68bf6ab8ea', '870d5ebb-b652-437b-b15d-55db95f07821')");
        queryRunner.update("delete from prosecution_organisation_access where case_id in ('b996b3cc-8613-4281-bdeb-bc480628e4d9', '5bc84b03-0fdc-49bd-bb47-b365ade0bb42', 'c0ea3f7f-ec71-4520-8581-2d68bf6ab8ea', '870d5ebb-b652-437b-b15d-55db95f07821', '08e1a52e-c7cf-4c69-80b9-3886c355f8ca', '8b3c08e3-99f4-487f-92e0-50d8fd24825c')");
        lines.stream()
                .map(line -> line.replace("DATE1", currentDate))
                .map(line -> line.replace("DATE2", pastDate))
                .forEach(line -> {
                    try {
                        queryRunner.insert(line, (ResultSetHandler<Object>) rs -> true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

    }
}
