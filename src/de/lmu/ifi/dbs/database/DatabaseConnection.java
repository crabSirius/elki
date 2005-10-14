package de.lmu.ifi.dbs.database;

import de.lmu.ifi.dbs.data.MetricalObject;
import de.lmu.ifi.dbs.normalization.Normalization;
import de.lmu.ifi.dbs.utilities.optionhandling.AttributeSettings;
import de.lmu.ifi.dbs.utilities.optionhandling.Parameterizable;

import java.util.List;

/**
 * DatabaseConnection is to provide a database. <p/> A database connection is to
 * manage the input and to provide a database where algorithms can run on. An
 * implementation may either use a parser to parse a sequential file or piped
 * input and provide a file based database or provide an intermediate connection
 * to a database system.
 * 
 * @author Arthur Zimek (<a
 *         href="mailto:zimek@dbs.ifi.lmu.de">zimek@dbs.ifi.lmu.de</a>)
 */
public interface DatabaseConnection<T extends MetricalObject> extends Parameterizable
{
    /**
     * Property key for available parsers.
     */
    public static final String PROPERTY_PARSER = "PARSER";

    /**
     * Property key for available databases.
     */
    public static final String PROPERTY_DATABASE = "DATABASE";

    /**
     * Returns a Database according to parameter settings.
     * 
     * @param normalization
     *            Normalization to perform a normalization if this action is
     *            supported. May remain null.
     * @return a Database according to parameter settings
     */
    Database<T> getDatabase(Normalization normalization);

    /**
     * Returns the setting of the attributes of this database connection.
     * 
     * @return the setting of the attributes of this database connection
     */
    public List<AttributeSettings> getAttributeSettings();
}
