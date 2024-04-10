package org.comroid.mcsd.api.dto.comm;

import lombok.Value;
import org.comroid.api.attr.Named;
import org.comroid.api.data.seri.DataNode;

@Value
public class ConsoleData implements DataNode {
    Type type;
    String data;
    public static ConsoleData input(String txt) {return new ConsoleData(Type.input,txt);}
    public static ConsoleData output(String txt) {return new ConsoleData(Type.output,txt);}
    public enum Type implements Named {input, output}
}
