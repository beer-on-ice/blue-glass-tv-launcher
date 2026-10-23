package com.doubao.glasshome;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;

final class HomeLogic {
    private static final java.util.regex.Pattern HDMI=java.util.regex.Pattern.compile("HDMI\\s*(\\d+)",java.util.regex.Pattern.CASE_INSENSITIVE);
    private static int portNumber(String label){
        java.util.regex.Matcher match=HDMI.matcher(label);
        if(match.find())try{return Integer.parseInt(match.group(1));}catch(NumberFormatException ignored){}
        return Integer.MAX_VALUE;
    }
    static int comparePorts(String a,String b){int order=Integer.compare(portNumber(a),portNumber(b));return order==0?a.compareToIgnoreCase(b):order;}
    static String inputName(String name,String fallback){
        String trimmed=name==null?"":name.trim();
        if(trimmed.isEmpty())return fallback;
        return trimmed.substring(0,trimmed.offsetByCodePoints(0,Math.min(24,trimmed.codePointCount(0,trimmed.length()))));
    }
    static List<String> inputTargets(String parent,List<String> children) {
        return children.isEmpty()?java.util.Collections.singletonList(parent):new ArrayList<>(new LinkedHashSet<>(children));
    }
    static List<Integer> firstPerPort(List<String> ports){
        List<Integer> indices=new ArrayList<>();LinkedHashSet<String> seen=new LinkedHashSet<>();
        for(int i=0;i<ports.size();i++)if(seen.add(ports.get(i)))indices.add(i);
        return indices;
    }
    static int[] step(int row, int column, int direction, int[] sizes) {
        if (direction == 0 || direction == 1)
            return new int[]{row, Math.max(0, Math.min(sizes[row]-1,column+(direction==0?-1:1)))};
        int delta = direction == 2 ? -1 : 1;
        int next = row + delta;
        while (next >= 0 && next < sizes.length && sizes[next] == 0) next += delta;
        if (next < 0 || next >= sizes.length) return new int[]{row,column};
        int mapped = (int)((column + .5) * sizes[next] / Math.max(1,sizes[row]));
        return new int[]{next, Math.min(sizes[next]-1,mapped)};
    }
    static List<String> favorites(List<String> preferred, List<String> available, int limit) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String name : preferred) if (available.contains(name) && result.size() < limit) result.add(name);
        for (String name : available) if (result.size() < limit) result.add(name);
        return new ArrayList<>(result);
    }
    static int inputState(int state, int portCount, int connectedCount) {
        return portCount > 1 && portCount == connectedCount ? -1 : state;
    }
}
