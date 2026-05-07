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
package com.webxells.dis.gis.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WktCenterTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        WktCenter fixture = new WktCenter();

        testCenter(fixture, "POINT EMPTY", "PoINT eMPty");
        String randomPoint = String.format("POINT(%s %s)", random(), random());
        testCenter(fixture, randomPoint, randomPoint);

        testCenter(fixture, "POINT(32.5 37.5)", "poLYGON ((40 40, 20 45, 45 30, 40 40 ))");
        testCenter(fixture, "POINT(32.5 37.5)", "poLYGON ((40 40, 20 45, 45 30, 40 40), (32 40, 21 40))");
        testCenter(fixture, "POINT EMPTY", "polygon empty");

        testCenter(fixture, "POINT(13.079581247960569 52.38854864572842)", "MULTIPOLYGON (((13.079042506939075 52.39039993432879, 13.079141006068333 52.39039606636156, 13.079201134722313 52.39039704372268, 13.079200939416205 52.3903937127954, 13.079317848239707 52.390389121729584, 13.07934981064555 52.39037935037276, 13.079359774338897 52.39038032499789, 13.079690625328725 52.39036605913986, 13.080639386324123 52.390325144623944, 13.08069710801849 52.3901541074449, 13.080800171663654 52.38992956413566, 13.081028854357005 52.38966396735841, 13.08128750992897 52.389362776937816, 13.081781186271392 52.38909160643996, 13.082042937406518 52.38894242972686, 13.08216117956251 52.38887504029314, 13.082552652595282 52.38864749159173, 13.082426951594659 52.388546460176514, 13.082387037878675 52.38851438006536, 13.081551340420265 52.38777239576615, 13.081903251021455 52.38726618349927, 13.08140622625971 52.38669735712805, 13.078746973235921 52.387542736812634, 13.078917661036375 52.38777739211357, 13.07902639148765 52.387926869329334, 13.07901145586372 52.387927487086365, 13.078277742884048 52.387957832071606, 13.078034465271754 52.38804014598041, 13.07667820086159 52.38939840651193, 13.076615840981303 52.389535424252394, 13.076609843325857 52.3896733593415, 13.07686969491625 52.3898587563963, 13.077240401223948 52.39008909183263, 13.077602640571381 52.39018988270094, 13.078190335432412 52.39029433920365, 13.078637498571213 52.390387880897485, 13.078947498416822 52.390392920783775, 13.07900522614933 52.39039385921434, 13.079042506939075 52.39039993432879)))");
        testCenter(fixture, "POINT(32.5 37.5)",
                "MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40), (32 40, 21 40), (32 40, 21 40)),\n" + "((20 35, 10 30, " +
                        "10 10, 30 5, 45 20, 20 35)))");

        testCenter(fixture, "POINT EMPTY", "multipolygon empty  ");

        assertThrows(InvalidDatasetException.class, () -> testCenter(fixture, null, "unsupported"));
    }

    void testCenter(WktCenter fixture, String expectation, String coordinates) throws InvalidDatasetException {
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config,
                        new SimpleMappingPoint(random(), random()), new SimpleMappingPoint(random(), random())) {{
                            getDataset().collect(new SimpleDatasetPiece(coordinates));
                }}
        ));
        fixture.setSource(SimpleMappingPortrayal.source(config.parts().get(0)));
        fixture.manipulate(config.parts().get(0).getDataset().getContent().get(0), config.parts().get(0));

        assertEquals(expectation, config.parts().get(0).value().get());

    }

}