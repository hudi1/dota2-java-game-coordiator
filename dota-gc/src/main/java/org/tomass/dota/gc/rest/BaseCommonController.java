package org.tomass.dota.gc.rest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseCommonController {

    private final Logger log = LoggerFactory.getLogger(BaseCommonController.class);

    private static final String PARAMETER_SPLIT = ";";
    private static final String VARARG_SPLIT = ",";

    protected String konfiguruj(String funkce, boolean bezKonverze, String args, Object invokedClass) {
        return konfigurujBatch(funkce, bezKonverze, args, null, invokedClass, null);
    }

    protected String konfiguruj(String funkce, boolean bezKonverze, String args, Object invokedClass,
            String oddelovace) {
        return konfigurujBatch(funkce, bezKonverze, args, null, invokedClass, oddelovace);
    }

    protected String konfigurujBatch(String funkce, boolean bezKonverze, String args, Integer batch,
            Object invokedClass, String oddelovace) {
        StringBuilder result = new StringBuilder();
        try {
            List<Object> parametry = parseArgs(args, bezKonverze, oddelovace);
            log.info("parameters " + parametry);
            if (batch == null) {
                result.append(zavolejFunkci(funkce, invokedClass, parametry.toArray()));
            } else {
                for (int i = 0; i < parametry.size(); i += batch) {
                    result.append(zavolejFunkci(funkce, invokedClass, parametry.subList(i, i + batch).toArray()));
                }
            }
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                result.append("ERROR: " + ((InvocationTargetException) e).getTargetException());
            } else {
                result.append("ERROR: " + e);

            }
        }
        return result.toString();
    }

    private String zavolejFunkci(String funkce, Object invokedClass, Object... parametry) throws Exception {
        StringBuilder result = new StringBuilder();
        result.append("Invoking method: " + funkce + " with args " + Arrays.toString(parametry));
        result.append(" and result: " + invokeMethod(funkce, invokedClass, parametry));
        return result.toString();
    }

    protected List<Object> parseArgs(String args, boolean bezKonverze, String oddelovace) {
        log.info("args " + args);
        if (args == null)
            return new ArrayList<>();

        String oddelovac = PARAMETER_SPLIT;
        String oddelovac2 = VARARG_SPLIT;
        if (oddelovace != null) {
            if (oddelovace.contains(" ")) {
                String[] ss = oddelovace.split(" ");
                if ("null".equalsIgnoreCase(ss[0]))
                    oddelovac = null;
                else
                    oddelovac = ss[0];
                if ("null".equalsIgnoreCase(ss[1]))
                    oddelovac2 = null;
                else
                    oddelovac2 = ss[1];
            } else {
                if ("null".equalsIgnoreCase(oddelovace))
                    oddelovac = null;
                else
                    oddelovac = oddelovace;
            }
        }
        log.info("delimeter " + oddelovac);

        Object[] parametry = { args };
        List<Object> hodnoty = new ArrayList<>();
        if (args != null) {
            if (oddelovac != null)
                parametry = args.split(oddelovac);
            hodnoty.addAll(Arrays.asList(parametry));
            String sparametr = (String) parametry[parametry.length - 1];
            if (oddelovac2 != null && sparametr.contains(oddelovac2)) {
                hodnoty.set(parametry.length - 1, sparametr.split(oddelovac2));
            }
        }
        for (int i = 0; i < hodnoty.size(); i++) {
            Object hodnota = hodnoty.get(i);
            if (hodnota instanceof String) {
                String parametr = (String) hodnota;
                if ("true".equalsIgnoreCase(parametr) || "false".equalsIgnoreCase(parametr)) {
                    hodnota = BooleanUtils.toBoolean(parametr);
                } else if ("null".equalsIgnoreCase(parametr)) {
                    hodnota = null;
                } else if (!bezKonverze && StringUtils.isNumeric(parametr)) {
                    hodnota = Integer.parseInt(parametr);
                } else if (!bezKonverze && NumberUtils.isCreatable(parametr)) {
                    hodnota = Long.parseLong(parametr.substring(0, parametr.length() - 1));
                }
            }
            hodnoty.set(i, hodnota);
        }
        return hodnoty;
    }

    private Object invokeMethod(String funkce, Object invokedClass, Object... parametry) throws Exception {
        Method[] methods = invokedClass.getClass().getMethods();
        for (Method method : methods) {
            if (funkce.equals(method.getName())) {
                if (method.getParameterCount() == parametry.length) {
                    method.setAccessible(true);
                    return method.invoke(invokedClass, parametry);
                }
            }
        }
        throw new NoSuchMethodException();
    }

}
