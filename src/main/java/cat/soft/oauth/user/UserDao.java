package cat.soft.oauth.user;


import cat.soft.oauth.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

import static cat.soft.oauth.util.EncryptionUtils.encryptSHA256;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    public User insertUser(User user) {
        String insertUserQuery = "insert into User (email, pw) values (?,?)";
        Object[] insertUserParams = new Object[]{user.getEmail(), encryptSHA256(user.getEmail())};

        this.jdbcTemplate.update(insertUserQuery, insertUserParams);

        String lastInsertEmail = this.jdbcTemplate.queryForObject("select last_insert_id()", String.class);

        user.setEmail(lastInsertEmail);

        return user;
    }

    public User selectByEmail(String email) {
        String selectByEmailQuery = "select email from User where email = ?";
        Object[] selectByEmailParams = new Object[]{email};
        try {
            return this.jdbcTemplate.queryForObject(selectByEmailQuery,
                    (rs, rowNum) -> new User(
                            rs.getString("email")),
                    selectByEmailParams);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int checkEmail(String email) {
        String checkEmailQuery = "select exists(select email from User where email = ?)";
        Object[] checkEmailParams = new Object[]{email};
        return this.jdbcTemplate.queryForObject(checkEmailQuery, int.class, checkEmailParams);
    }
}
