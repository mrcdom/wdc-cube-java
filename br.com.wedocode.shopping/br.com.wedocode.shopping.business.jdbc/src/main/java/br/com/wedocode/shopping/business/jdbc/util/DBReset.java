package br.com.wedocode.shopping.business.jdbc.util;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Calendar;

import org.apache.commons.io.IOUtils;

import br.com.wedocode.framework.commons.util.StringUtils;

public class DBReset {

    public static long ADMIN_ID;
    public static long FULANO_ID;
    public static long BEOTRANO_ID;

    public static long CAFETEIRA_ID;
    public static long BOLA_WILSON_ID;
    public static long FITA_VEDA_ROSCA_ID;
    public static long PEN_DRIVE2GB_ID;

    public static long ADMIN_FIRST_PURCHASE_ID;
    public static long ADMIN_FIRST_PURCHASE_ITEM0_ID;

    public static long ADMIN_SECOND_PURCHASE_ID;
    public static long ADMIN_SECOND_PURCHASE_ITEM0_ID;
    public static long ADMIN_SECOND_PURCHASE_ITEM1_ID;

    public static void run(final Connection c) throws Exception {
        class Util {

            PreparedStatement statement;

            void alterSequence(String name, long value) throws Exception {
                Statement stmt = null;
                try {
                    stmt = c.createStatement();
                    stmt.execute("ALTER SEQUENCE " + name + " RESTART WITH " + value);
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
            }

            void addUser(long ID, String USERNAME, String PASSWORD, String NAME) throws Exception {
                this.statement.setLong(1, ID);
                this.statement.setString(2, USERNAME);
                if (StringUtils.isNotBlank(PASSWORD)) {
                    var pwd = new BigInteger(MessageDigest.getInstance("MD5").digest(PASSWORD.getBytes("UTF-8")))
                            .toString(36);
                    this.statement.setString(3, pwd);
                } else {
                    this.statement.setString(3, null);
                }
                this.statement.setString(4, NAME);
                this.statement.execute();
            }

            void addProduct(long ID, String NAME, double PRICE, String DESCRIPTION, String IMAGE) throws Exception {
                this.statement.setLong(1, ID);
                this.statement.setString(2, NAME);
                this.statement.setDouble(3, PRICE);

                if (StringUtils.isNotBlank(DESCRIPTION)) {
                    this.statement.setBytes(4, DESCRIPTION.getBytes("UTF-8"));
                } else {
                    this.statement.setBytes(4, null);
                }

                InputStream imageStream = IMAGE != null ? DBReset.class.getResourceAsStream("/META-INF/" + IMAGE)
                        : null;
                if (imageStream != null) {
                    try {
                        final byte[] bytes = IOUtils.toByteArray(imageStream);
                        this.statement.setBytes(5, bytes);
                    } finally {
                        imageStream.close();
                    }
                } else {
                    this.statement.setBytes(5, null);
                }

                this.statement.execute();
            }

            public void addPurchase(long ID, long USERID, int[] date) throws Exception {
                final Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, date[0]);
                cal.set(Calendar.MONTH, date[1] - 1);
                cal.set(Calendar.DAY_OF_MONTH, date[2]);

                this.statement.setLong(1, ID);
                this.statement.setLong(2, USERID);
                this.statement.setDate(3, new java.sql.Date(cal.getTimeInMillis()));
                this.statement.execute();
            }

            public void addPurchaseItem(long ID, long PURCHASEID, long PRODUCTID, int AMOUNT, double PRICE)
                    throws Exception {
                this.statement.setLong(1, ID);
                this.statement.setLong(2, PURCHASEID);
                this.statement.setLong(3, PRODUCTID);
                this.statement.setInt(4, AMOUNT);
                this.statement.setDouble(5, PRICE);
                this.statement.execute();
            }

        }
        ;
        final Util util = new Util();

        PreparedStatement statement = null;
        try {
            /*
             * Clean all
             */

            for (final String tbName : new String[] { "EN_PURCHASEITEM", "EN_PURCHASE", "EN_PRODUCT", "EN_USER" }) {
                statement = c.prepareStatement("DELETE FROM " + tbName);
                statement.execute();
                statement.close();
                statement = null;
            }

            long id;

            /*
             * Users
             */

            statement = util.statement = c
                    .prepareStatement("INSERT INTO EN_USER(ID, USERNAME, PASSWORD, NAME) VALUES (?, ?, ?, ?)");

            id = 0;
            util.addUser(DBReset.ADMIN_ID = id++, "admin", "admin", "João da Silva");
            util.addUser(DBReset.FULANO_ID = id++, "fulano", "fulano", "Fulano de Tal");
            util.addUser(DBReset.BEOTRANO_ID = id++, "beotrano", "beotrano", "Beotrano de Alguma Coisa");

            util.alterSequence("SQ_USER", id);

            /*
             * Products
             */

            statement = util.statement = c.prepareStatement(
                    "INSERT INTO EN_PRODUCT(ID, NAME, PRICE, DESCRIPTION, IMAGE)" + " VALUES (?, ?, ?, ?, ?)");

            id = 0;
            util.addProduct(DBReset.CAFETEIRA_ID = id++, "Cafeteira design italiano", 199.99,
                    "<ul>" + "<li>Capacidade para 30 cafés (50ml cada) ou 24 cafés (62ml cada)</li>"
                            + "<li>Sistema corta-pingos</li>"
                            + "<li>Acompanha filtro permanente removível e colher medidora</li>"
                            + "<li>Permite uso de filtro de papel</li>"
                            + "<li>Reservatório de água com graduação</li>" + "<li>Botão luminoso liga/desliga</li>"
                            + "<li>Fácil de lavar</li>"
                            + "<li>Peças podem ser lavadas em máquina de lavar louça (exceto a base motora)</li>"
                            + "<li>Potência: 1000W - correspondente a 1 Kwh (Kilowatts hora).</li>" + "</ul>",
                    "images/cafeteira.png");

            util.addProduct(DBReset.BOLA_WILSON_ID = id++, "Bola Wilson", 45.30,
                    "<ul>" + "<li>Bola Wilson Tamanho e Peso Oficial.</li>"
                            + "<li>Garantia: Contra defeito de fabricação.</li>" + "<li>Origem: Importada.</li>"
                            + "</ul>",
                    "images/wilson.png");

            util.addProduct(DBReset.FITA_VEDA_ROSCA_ID = id++, "Fita veda rosca", 2.67,
                    "<ul>" + "<li>Marca Tigre.</li>" + "<li>Tamanho e medida: 18 mm x 10 m.</li>"
                            + "<li>Composição: Teflon.</li>" + "<li>Utilização: vedação de juntas roscaveis.</li>"
                            + "</ul>",
                    "images/vedarosca.png");

            util.addProduct(DBReset.PEN_DRIVE2GB_ID = id++, "Pen Drive 2GB", 16.0,
                    "Ideal para transporte de arquivos de dados, áudio, vídeo, "
                            + "fotos e muito mais. Melhor valor para armazenamento e transferência de informação. Portátil, "
                            + "fácil de usar e super leve, ele possui segurança com seus dados, led indicando o uso, além "
                            + "de ser resistente a quedas. Pen Drive com capacidade de armazenamento de 2 GB, praticidade "
                            + "e qualidade com seus arquivos!",
                    "images/pendrive2gb.png");

            util.alterSequence("SQ_PRODUCT", id);

            /*
             * Purchases
             */

            statement = util.statement = c
                    .prepareStatement("INSERT INTO EN_PURCHASE(ID, USERID, BUYDATE) VALUES (?, ?, ?)");

            id = 0;
            util.addPurchase(DBReset.ADMIN_FIRST_PURCHASE_ID = id++, DBReset.ADMIN_ID, new int[] { 2010, 1, 1 });
            util.addPurchase(DBReset.ADMIN_SECOND_PURCHASE_ID = id++, DBReset.ADMIN_ID, new int[] { 2011, 4, 3 });

            util.alterSequence("SQ_PURCHASE", id);

            /*
             * Purchases itens
             */

            statement = util.statement = c
                    .prepareStatement("INSERT INTO EN_PURCHASEITEM(ID, PURCHASEID, PRODUCTID, AMOUNT, PRICE)"
                            + " VALUES (?, ?, ?, ?, ?)");

            id = 0;
            util.addPurchaseItem(DBReset.ADMIN_FIRST_PURCHASE_ITEM0_ID = id++, DBReset.ADMIN_FIRST_PURCHASE_ID,
                    DBReset.CAFETEIRA_ID, 1, 200.0);

            util.addPurchaseItem(DBReset.ADMIN_SECOND_PURCHASE_ITEM0_ID = id++, DBReset.ADMIN_SECOND_PURCHASE_ID,
                    DBReset.BOLA_WILSON_ID, 1, 45.30);

            util.addPurchaseItem(DBReset.ADMIN_SECOND_PURCHASE_ITEM1_ID = id++, DBReset.ADMIN_SECOND_PURCHASE_ID,
                    DBReset.FITA_VEDA_ROSCA_ID, 1, 2.67);

            util.alterSequence("SQ_PURCHASEITEM", id);
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

}
