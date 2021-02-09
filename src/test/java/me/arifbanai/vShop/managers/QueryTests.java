package me.arifbanai.vShop.managers;

import me.arifbanai.easypool.DataSourceManager;
import me.arifbanai.easypool.MySQLDataSourceManager;
import me.arifbanai.easypool.SQLiteDataSourceManager;
import me.arifbanai.vShop.managers.sql.SqlQueries;
import me.arifbanai.vShop.objects.Offer;
import me.arifbanai.vShop.utils.Config;
import me.arifbanai.vShop.utils.Database;
import me.arifbanai.vShop.utils.TestUtils;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

/**
 * Tests for the queries used by vShop
 *
 * Set a system property "password" to the DB password
 */
@SuppressWarnings("FieldMayBeFinal")
public class QueryTests {

    private final DataSourceManager dsm;
    private final SqlQueries sqlQueries;

    private final ArrayList<String> playerUUIDs;
    private List<Material> materials;

    private static final String RESOURCES_DIR = "src/test/resources/";
    private static final String CONFIG_FILENAME = "config.yml";
    private static final String UUIDS_FILENAME = "playerUUIDs.txt";

    private QueryTests() throws IOException, SQLException {
        dsm = setupDataSourceManager(System.getProperty("password"));
        sqlQueries = new SqlQueries(dsm);

        sqlQueries.prepareDB();

        playerUUIDs = new ArrayList<>(Files.readAllLines(Paths.get(RESOURCES_DIR + UUIDS_FILENAME)));
        materials = Arrays.asList(Material.values());
    }

    // b2350ccc-6103-4820-8bb6-724f30f71372 = Fuzzlr

    /**
     * Create some number offers and add them to the DB, in parallel using Parallel Stream
     * @throws SQLException
     */
    @Test
    public void addOffers() {

        long startTime = System.nanoTime();
        Set<Offer> offers = createOffers(playerUUIDs.size(), 100);

        offers.parallelStream().forEach(offer -> {
            try {
                sqlQueries.addOffer(offer);
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        });

        long endTime = System.nanoTime();
        long durationInMilliseconds = (endTime - startTime) / 1000000;

        dsm.close();

        String methodName = "addOffers(" + playerUUIDs.size() * 100 + ") ";
        System.out.println(methodName + "ran in " + durationInMilliseconds + "ms");
    }

    @Test
    public void getOfferAmount() throws SQLException {
        String textID = "Item to test";

        long startTime = System.nanoTime();
        int amount = sqlQueries.getAmountOfItemOffers(textID);
        long endTime = System.nanoTime();

        long durationInMilliseconds = (endTime - startTime) / 1000000;
        dsm.close();

        String methodNameAndResult = "getOfferAmount(" + textID + ") = " + amount;
        System.out.println(methodNameAndResult);
        System.out.println("Ran in " + durationInMilliseconds + "ms");
    }

    /**
     * Generate unique offers
     * Number of offers created = numSellers * numItems
     * @param numSellers - the number of sellers
     * @param numItems - the number of items to be sold by each seller
     * @return (numSellers * numItems) unique offers
     */
    private Set<Offer> createOffers(int numSellers, int numItems) {

        //TODO Provide playerUUIDs, materials, amounts, and prices as arguments

        Set<Offer> offers = new HashSet<>(numSellers * numItems);

        List<String> uuids = playerUUIDs.subList(0, numSellers);
        List<Material> randomMaterials = TestUtils.getRandomSubList(materials, numItems);

        int numOfOffers = numSellers * numItems;

        int[] amounts = new Random().ints(numOfOffers,
                        1,
                        100).toArray();

        double[] prices = new Random().doubles(numOfOffers,
                        1,
                        1000).toArray();

        PrimitiveIterator<Integer, IntConsumer> amountIterator = Arrays.stream(amounts).iterator();
        PrimitiveIterator<Double, DoubleConsumer> priceIterator = Arrays.stream(prices).iterator();

        for(String uuid : uuids) {
            for(Material randomMat : randomMaterials) {
                int amount = amountIterator.next();
                double price =  priceIterator.next();

                Offer o = new Offer(uuid, randomMat.toString(), amount, price);

                offers.add(o);
            }
        }

        System.out.println("Created " + offers.size() + " offers");

        Assertions.assertEquals(numSellers * numItems, offers.size());

        return offers;
    }

    /**
     * Setup the DSM with some RDBMS specified in config
     * TODO Add support for SQLite and (eventually) others
     * @return the DataSourceManager
     */
    private static DataSourceManager setupDataSourceManager(String password) throws IOException {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        File configFile = new File(RESOURCES_DIR + CONFIG_FILENAME);
        InputStream inputStream = new FileInputStream(configFile);

        Config config = yaml.load(inputStream);
        Database db = config.getDb();

        if(config.isUsingSQLite()) {
            return new SQLiteDataSourceManager(RESOURCES_DIR, "vShop3.0");
        } else {
            String host = db.getHost();
            String port = db.getPort();
            String schema = db.getSchema();
            String user = db.getUsername();
            //TODO String dialect = config.getString("db.dialect");
            //TODO Use a switch to handle multiple sql dialects

            return new MySQLDataSourceManager(host, port, schema, user, password);

        }
    }
}
