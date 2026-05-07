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

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.Registry;

public class PrjFileParser {
    private record Element(String name, List<String> values) { }

    private static final Logger LOGGER = LoggerProxyFactory.logger(PrjFileParser.class);
    private static final List<String> KNOWN_PRIMEM_NAMES = List.of("greenwich", "lisbon", "paris", "bogota",
            "madrid", "rome", "bern", "jakarta", "ferro", "brussels", "stockholm", "athens", "oslo");
    private static final List<String> UTM_SYSTEMS = List.of("utm", "tmerc", "merc");
    private static final List<String> VALID_DATUM = Arrays.stream(Registry.datums)
            .map(a -> a.getCode().toLowerCase(Locale.ROOT))
            .toList();

    private final List<Element> elements = new ArrayList<>();
    private int currentPosition;
    private String currentProjection;

    public static CoordinateReferenceSystem parseToLocationtech(String wkt, final CRSFactory crsFactory) throws IOException {
        final PrjFileParser parser = new PrjFileParser(wkt.toLowerCase(Locale.ROOT));
        return crsFactory.createFromParameters(null, parser.toProj(true));
    }

    public static CoordinateReferenceSystem parse(String wkt, final CRSFactory crsFactory) throws IOException {
        final PrjFileParser parser = new PrjFileParser(wkt.toLowerCase(Locale.ROOT));
        return crsFactory.createFromParameters(null, parser.toProj(false));
    }

    private String[] toProj(final boolean filterOutdatedDatum) {
        final List<String> result = new ArrayList<>(parseParameters());
        final Optional<String> datum = parseDatum(filterOutdatedDatum);
        final List<String> spheroid = parseSpheroid();
        datum.ifPresent(result::add);
        result.addAll(parseProjection(datum.isEmpty() && spheroid.isEmpty()));
        parseUnit()
                .ifPresent(result::add);
        result.addAll(spheroid);
        parsePrimem()
                .map("+pm="::concat)
                .ifPresent(result::add);
        parseTowgs84().ifPresent(result::add);
        result.add("+no_defs");
        return result.toArray(new String[0]);
    }

    private Optional<String> parseTowgs84() {
        final Element towgs84 = findUnique("TOWGS84");
        if (towgs84 == null || 3 > towgs84.values.size()) {
            return Optional.empty();
        }
        return Optional.of("+towgs84=".concat(String.join(",", towgs84.values)));
    }

    private List<String> parseSpheroid() {
        final Element spheroid = findUnique("spheroid");
        if (spheroid == null || spheroid.values().isEmpty()) {
            return List.of();
        }
        final Optional<String> knownName = Optional.of(spheroid.values.getFirst())
                .map(a -> switch (a) {
                    case "wgs_1984", "wgs_84" -> "WGS84";
                    case "grs_1980", "grs_80" -> "GRS80";
                    case "airy", "airy_1830", "airy_30" -> "airy";
                    case "bessel_1841" -> "bessel";
                    case "clarke_1866", "clarke_66" -> "clrk66";
                    case "clarke_1880", "clarke_80" -> "clrk80";
                    case "grs80_modified" -> "denali";
                    case "international", "international_1924", "international_24" -> "intl";
                    case "australian_national_spheroid" -> "aust_SA";
                    case "helmert_1906", "helmert_06" -> "helmert";
                    case "hough" -> "hough";
                    case "everest_1830", "everest_30" -> "evrst30";
                    case "everest_1948", "everest_48" -> "evrst48";
                    case "wgs_1972", "wgs_72" -> "wgs72";
                    case "sphere" -> "sphere";
                    case "airy_modified" -> "mod_airy";
                    case "everest_modified" -> "mod_evrs";
                    case "sgs_1985", "sgs_85" -> "SGS85";
                    case "grs_1967", "grs_67" -> "GRS67";
                    case "bessel_namibia" -> "bess_nam";
                    case "wgs_1960", "wgs_60" -> "WGS60";
                    case "krassowsky", "krassowsky_1940", "krassowsky_40" -> "krassowsky";
                    default -> null;
                });
        if (knownName.isPresent()) {
            return List.of("+ellps=".concat(knownName.get()));
        }
        if (2 < spheroid.values.size()) {
            return List.of("+a=".concat(spheroid.values.get(1)), "+rf=".concat(spheroid.values.get(2)));
        }
        return List.of();
    }

    private Optional<String> parsePrimem() {
        final Element primem = findUnique("primem");
        if (!(primem == null || primem.values().isEmpty())) {
            final String name = primem.values().getFirst();
            if (KNOWN_PRIMEM_NAMES.contains(name)) {
                return Optional.of(name);
            }
            if (Character.isDigit(name.charAt(0))) {
                return Optional.of(name);
            }
            if (1 < primem.values().size()) {
                return Optional.of(primem.values().get(1));
            }
        }
        return Optional.empty();
    }

    private List<String> parseProjection(final boolean noDatumOrSpheroid) {
        if (noDatumOrSpheroid) {
            return List.of("+proj=longlat", "+datum=WGS84");
        }
        final Element projection = findUnique("projection");
        final List<String> result = new ArrayList<>();
        if (projection != null && 0 < projection.values.size()) {
            Optional.of(projection.values.getFirst())
                    .map(a -> switch (a) {
                        case "albers_equal_area" -> "aea";
                        case "azimuthal_equidistant" -> "aeqd";
                        case "cassini-soldner" -> "cass";
                        case "cylindrical_equal_area" -> "cea";
                        case "equidistant_conic" -> "eqdc";
                        case "equirectangular_(plate_carrée)", "equirectangular" -> "eqc";
                        case "eckert_i" -> "eck1";
                        case "eckert_ii" -> "eck2";
                        case "eckert_iii" -> "eck3";
                        case "eckert_iv" -> "eck4";
                        case "eckert_v" -> "eck5";
                        case "eckert_vi" -> "eck6";
                        case "gauss-krüger", "transverse_mercator" -> "tmerc";
                        case "gnomonic" -> "gnom";
                        case "hammer" -> "hammer";
                        case "krovak" -> "krovak";
                        case "lambert_azimuthal_equal_area" -> "laea";
                        case "lambert_conformal_conic_(1sp)", "lambert_conformal_conic" -> "lcc";
                        case "longlat" -> "longlat";
                        case "mercator_(1sp)", "mercator" -> "merc";
                        case "miller_cylindrical" -> "mill";
                        case "mollweide" -> "moll";
                        case "new_zealand_map_grid" -> "nzmg";
                        case "oblique_stereographic" -> "sterea";
                        case "orthographic" -> "ortho";
                        case "polar_stereographic", "stereographic" -> "stere";
                        case "polyconic" -> "poly";
                        case "robinson" -> "robin";
                        case "sinusoidal" -> "sinu";
                        case "swiss_oblique_mercator" -> "somerc";
                        case "universal_transverse_mercator", "utm" -> "utm";
                        case "van_der_grinten" -> "vandg";
                        case "wagner_ii" -> "wag2";
                        case "wagner_iii" -> "wag3";
                        case "wagner_iv" -> "wag4";
                        case "wagner_v" -> "wag5";
                        case "wagner_vi" -> "wag6";
                        case "wagner_vii" -> "wag7";
                        default -> throw new IllegalStateException("Unexpected projection: " + a);
                    })
                    .ifPresent(a -> {
                        currentProjection = a;
                        result.add("+proj=".concat(a));
                    });
        } else {
            result.add("+proj=longlat");
        }
        return result;
    }

    private Optional<String> parseDatum(final boolean tryUpdateDeprecated) {
        final Element datum = findUnique("datum");
        if (null == datum || datum.values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(replaceOptionalPrefix("d", datum.values.getFirst()))
                .map(a -> switch (a) {
                    case "wgs_1984" -> "WGS84";
                    case "potsdam_1950" -> "potsdam";
                    case "osgb_1936" -> "OSGB36";
                    case "tananarive_1925" -> "tananarive";
                    case "irenet95" -> "ire65";
                    case "monte_mario" -> "rome40";
                    case "hermannskoj" -> "hermannskogel";
                    case "reseau_national_belge_1972" -> "belge72";
                    default -> tryTranslateDatumByRules(a);
                })
                .filter(a -> !tryUpdateDeprecated || VALID_DATUM.contains(a.toLowerCase(Locale.ROOT)))
                .map("+datum="::concat);
    }

    private String tryTranslateDatumByRules(final String value) {
        final StringBuilder result = new StringBuilder();
        final int valueLength = value.length();
        if (0 < value.indexOf('_')) {
            if (value.indexOf('_') < value.lastIndexOf('_')) {
                for (int i = 0; i < valueLength && -1 < i && !Character.isDigit(value.charAt(i + 1));
                     i = value.indexOf('_', i + 1)) {
                    if ("of".equals(value.substring(i + 1, value.indexOf('_', i + 1)))) {
                        continue;
                    }
                    result.append(value.charAt(i == 0 ? 0 : i + 1));
                }
            } else {
                result.append(value, 0, value.indexOf('_'));
            }
            final String lastPart = value.substring(value.lastIndexOf('_') + 1);
            if (4 == lastPart.length() && lastPart.startsWith("19")) {
                result.append(lastPart.substring(2));
            } else {
                result.append(lastPart);
            }
        } else {
            LOGGER.w("Unknown WKT datum: %s", value);
            result.append(value);
        }
        return result.toString();
    }

    private String replaceOptionalPrefix(final String prefix, final String value) {
        if (value.startsWith(prefix.concat("_"))) {
            return value.substring(prefix.length() + 1);
        }
        return value;
    }

    private Optional<String> parseUnit() {
        final Element unit = findUnique("unit");
        if (null == unit || unit.values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unit.values.getFirst())
                .map(a -> switch(a) {
                    case "meter" -> "m";
                    case "degree" -> "degrees";
                    default -> throw new IllegalStateException("Unexpected unit: " + unit.values.getFirst());
                })
                .map(a -> null != currentProjection && UTM_SYSTEMS.contains(currentProjection) ? "m" : a)
                .map("+units="::concat);
    }

    private Element findUnique(final String name) {
        final List<Element> result = findAll(name);
        if (1 < result.size()) {
            LOGGER.w("Parameter %s should be unique but spotted %d times - take first", name, result.size());
        }
        return result.isEmpty() ? null : result.getFirst();
    }

    private List<String> parseParameters() {
        final List<String> result = new ArrayList<>();
        final List<Element> parameters = findAll("parameter");
        for (final Element current : parameters) {
            if (current.values.size() < 2) {
                throw new IllegalArgumentException("Missing parameter");
            }
            Optional.of(current.values.getFirst())
                    .map(a -> switch (a) {
                            case "latitude_of_origin", "latitude_of_center" -> "lat_0";
                            case "central_meridian", "longitude_of_center", "straight_vertical_longitude_from_pole" -> "lon_0";
                            case "scale_factor" -> "k";
                            case "false_easting" -> "x_0";
                            case "false_northing" -> "y_0";
                            case "azimuth", "azimuth_of_central_line" -> "azi";
                            case "perspective_point_height", "viewpoint_height" -> "h";
                            case "latitude_of_standard_parallel", "standard_parallel" -> "lat_ts";
                            case "latitude_of_standard_parallel_1", "standard_parallel_1" -> "lat_1";
                            case "latitude_of_standard_parallel_2", "standard_parallel_2" -> "lat_2";
                            default -> throw new IllegalArgumentException("Unknown parameter: " + a);
                    })
                    .ifPresent(a -> result.add(String.format("+%s=%s", a, current.values.get(1))));
        }
        return result;
    }

    private List<Element> findAll(final String name) {
        return elements.stream()
                .filter(a -> a.name.equals(name))
                .toList();
    }

    private PrjFileParser(final String wkt) throws IOException {
        final int wktLength = wkt.length();
        for (currentPosition = 0; currentPosition < wktLength;) {
            currentPosition = skipClosing(wkt, currentPosition);
            final int nameEnd = wkt.indexOf('[', currentPosition) + 1;
            if (0 == nameEnd) {
                break;
            }
            final Element current = new Element(
                    wkt.substring(currentPosition, nameEnd - 1).trim(), new ArrayList<>());
            elements.add(current);
            parseValues(wkt, current.values, skipWhiteSpace(wkt, nameEnd));
        }
    }

    private void parseValues(final String wkt, final List<String> values, int valueStart) throws IOException {
        while (true) {
            if (Character.isAlphabetic(wkt.charAt(valueStart))) {
                //new name starting: new Element
                currentPosition = valueStart;
                return;
            }
            if (']' == wkt.charAt(valueStart)) {
                //closing statement
                currentPosition = valueStart + 1;
                return;
            }
            values.add(parseValue(wkt, valueStart));
            valueStart = skipWhiteSpace(wkt, currentPosition);
            if (',' == wkt.charAt(valueStart)) {
                valueStart = skipWhiteSpace(wkt, valueStart + 1);
                continue;
            }
            return;
        }
    }

    private String parseValue(final String wkt, final int valueStart) throws IOException {
        if ('"' == wkt.charAt(valueStart)) {
            //string
            currentPosition = wkt.indexOf('"', valueStart + 1) + 1;
            return wkt.substring(valueStart + 1, currentPosition - 1);
        } else if (Character.isDigit(wkt.charAt(valueStart))) {
            //double
            boolean gotPoint = false;
            int step = 1;
            while (Character.isDigit(wkt.charAt(valueStart + step)) || (!gotPoint && (gotPoint = '.' == wkt.charAt(valueStart + step)))) {
                step++;
            }
            currentPosition = valueStart + step;
            return wkt.substring(valueStart, currentPosition);
        }  else {
            //unexpected
            LOGGER.d("Current on index %d in wkt: %s", currentPosition, wkt);
            throw new IOException("Invalid char as prj value: " + wkt.charAt(valueStart));
        }
    }

    private int skipClosing(final String string, final int pos) {
        return skip(string, pos, current -> ']' == current || ',' == current);
    }

    private int skipWhiteSpace(final String string, final int pos) {
        return skip(string, pos, Character::isWhitespace);
    }

    private int skip(final String string, final int pos, final Function<Character, Boolean> checkFunction) {
        int skip = 0;
        int m = string.length();
        while (pos + skip < m && checkFunction.apply(string.charAt(pos + skip))) {
            skip++;
        }
        return pos + skip;
    }
}