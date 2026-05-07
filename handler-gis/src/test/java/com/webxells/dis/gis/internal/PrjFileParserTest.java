/**
 * Copyright (C) 2020-2026 webXells GmbH
 *
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://creativecommons.org/licenses/by-nc-nd/4.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 **/
package com.webxells.dis.gis.internal;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.locationtech.proj4j.CRSFactory;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class PrjFileParserTest {

    @Test
    void testParse() throws IOException {
        testParse("GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]]",
                "+datum=WGS84", "+proj=longlat", "+units=degrees", "+ellps=WGS84", "+pm=greenwich", "+no_defs");

        testParse("PROJCS[\"GDA94 / MGA zone 56\", GEOGCS[\"GDA94\", DATUM[\"Geocentric_Datum_of_Australia_1994\", SPHEROID[\"GRS 1980\",6378137,298.257222101]], PRIMEM[\"Greenwich\",0], UNIT[\"degree\",0.0174532925199433]], PROJECTION[\"Transverse_Mercator\"], PARAMETER[\"latitude_of_origin\",0], PARAMETER[\"central_meridian\",153], PARAMETER[\"scale_factor\",0.9996], PARAMETER[\"false_easting\",500000], PARAMETER[\"false_northing\",10000000], UNIT[\"Meter\",1]]",
                "+lat_0=0", "+lon_0=153", "+k=0.9996", "+x_0=500000", "+y_0=10000000", "+datum=gda94", "+proj=tmerc", "+units=m", "+a=6378137", "+rf=298.257222101", "+pm=greenwich", "+no_defs");

        testParse("PROJCS[\"Belge_Lambert_1972\",GEOGCS[\"GCS_Belge_1972\",DATUM[\"D_Belge_1972\",SPHEROID[\"International_1924\",6378388.0,297.0]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Lambert_Conformal_Conic\"],PARAMETER[\"False_Easting\",150000.013],PARAMETER[\"False_Northing\",5400088.438],PARAMETER[\"Central_Meridian\",4.36748666666667],PARAMETER[\"Standard_Parallel_1\",51.1666672333333],PARAMETER[\"Standard_Parallel_2\",49.8333339],PARAMETER[\"Latitude_Of_Origin\",90.0],UNIT[\"Meter\",1.0]]",
                "+x_0=150000.013", "+y_0=5400088.438", "+lon_0=4.36748666666667", "+lat_1=51.1666672333333", "+lat_2=49.8333339", "+lat_0=90.0", "+datum=belge72", "+proj=lcc", "+units=degrees", "+ellps=intl", "+pm=greenwich", "+no_defs");

        testParse("PROJCS[\"ETRS_1989_UTM_Zone_33N\",GEOGCS[\"GCS_ETRS_1989\",DATUM[\"D_ETRS_1989\",SPHEROID[\"GRS_1980\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",15.0],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]]",
                "+x_0=500000.0", "+y_0=0.0", "+lon_0=15.0", "+k=0.9996", "+lat_0=0.0", "+datum=etrs89", "+proj=tmerc", "+units=m", "+ellps=GRS80", "+pm=greenwich", "+no_defs");
    }

    @Test
    void testParseToLocationtech() throws IOException {
        testParseToLocationtech("GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]]",
                "+datum=WGS84", "+proj=longlat", "+units=degrees", "+ellps=WGS84", "+pm=greenwich", "+no_defs");

        testParseToLocationtech("PROJCS[\"GDA94 / MGA zone 56\", GEOGCS[\"GDA94\", DATUM[\"Geocentric_Datum_of_Australia_1994\", SPHEROID[\"GRS 1980\",6378137,298.257222101]], PRIMEM[\"Greenwich\",0], UNIT[\"degree\",0.0174532925199433]], PROJECTION[\"Transverse_Mercator\"], PARAMETER[\"latitude_of_origin\",0], PARAMETER[\"central_meridian\",153], PARAMETER[\"scale_factor\",0.9996], PARAMETER[\"false_easting\",500000], PARAMETER[\"false_northing\",10000000], UNIT[\"Meter\",1]]",
                "+lat_0=0", "+lon_0=153", "+k=0.9996", "+x_0=500000", "+y_0=10000000", "+proj=tmerc", "+units=m", "+a=6378137", "+rf=298.257222101", "+pm=greenwich", "+no_defs");

        testParseToLocationtech("PROJCS[\"Belge_Lambert_1972\",GEOGCS[\"GCS_Belge_1972\",DATUM[\"D_Belge_1972\",SPHEROID[\"International_1924\",6378388.0,297.0]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Lambert_Conformal_Conic\"],PARAMETER[\"False_Easting\",150000.013],PARAMETER[\"False_Northing\",5400088.438],PARAMETER[\"Central_Meridian\",4.36748666666667],PARAMETER[\"Standard_Parallel_1\",51.1666672333333],PARAMETER[\"Standard_Parallel_2\",49.8333339],PARAMETER[\"Latitude_Of_Origin\",90.0],UNIT[\"Meter\",1.0]]",
                "+x_0=150000.013", "+y_0=5400088.438", "+lon_0=4.36748666666667", "+lat_1=51.1666672333333", "+lat_2=49.8333339", "+lat_0=90.0", "+proj=lcc", "+units=degrees", "+ellps=intl", "+pm=greenwich", "+no_defs");

        testParseToLocationtech("PROJCS[\"ETRS_1989_UTM_Zone_33N\",GEOGCS[\"GCS_ETRS_1989\",DATUM[\"D_ETRS_1989\",SPHEROID[\"GRS_1980\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",15.0],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]]",
                "+x_0=500000.0", "+y_0=0.0", "+lon_0=15.0", "+k=0.9996", "+lat_0=0.0", "+proj=tmerc", "+units=m", "+ellps=GRS80", "+pm=greenwich", "+no_defs");
    }

    private void testParse(final String wkt, final String... expected) throws IOException {
        CRSFactory factory = Mockito.mock(CRSFactory.class);

        PrjFileParser.parse(wkt, factory);

        Mockito.verify(factory).createFromParameters(ArgumentMatchers.isNull(),
                ArgumentMatchers.eq(expected));
    }

    private void testParseToLocationtech(final String wkt, final String... expected) throws IOException {
        CRSFactory factory = Mockito.mock(CRSFactory.class);

        PrjFileParser.parseToLocationtech(wkt, factory);

        Mockito.verify(factory).createFromParameters(ArgumentMatchers.isNull(),
                ArgumentMatchers.eq(expected));
    }

}