package dev.nathanlively.crosslite_r1_eq.writer;

import dev.nathanlively.crosslite_r1_eq.domain.R1Filter;
import dev.nathanlively.crosslite_r1_eq.domain.R1Settings;
import org.springframework.stereotype.Component;

/**
 * Writes R1 settings to XML format.
 */
@Component
public class R1Writer {
    
    public String writeToXml(R1Settings settings) {
        StringBuilder xml = new StringBuilder();
        xml.append("<R1EQSETTINGS_20><EQ><REMARKS></REMARKS><EQx_ON>1.0</EQx_ON>");
        
        for (int i = 0; i < settings.filters().size(); i++) {
            R1Filter filter = settings.filters().get(i);
            int filterNum = i + 1;
            
            xml.append("<FILTER_").append(filterNum).append(">1.000000</FILTER_").append(filterNum).append(">");
            xml.append("<F_").append(filterNum).append(">").append(String.format("%.6f", filter.frequency())).append("</F_").append(filterNum).append(">");
            xml.append("<Q_").append(filterNum).append(">").append(String.format("%.6f", filter.qFactor())).append("</Q_").append(filterNum).append(">");
            xml.append("<G_").append(filterNum).append(">").append(String.format("%.6f", filter.gain())).append("</G_").append(filterNum).append(">");
            xml.append("<E_").append(filterNum).append(">").append(filter.enabled() ? "1.000000" : "0.000000").append("</E_").append(filterNum).append(">");
        }
        
        xml.append("</EQ></R1EQSETTINGS_20>");
        return xml.toString();
    }
}