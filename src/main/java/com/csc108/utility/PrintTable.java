package com.csc108.utility;

import java.util.HashMap;
import java.util.Map;
import com.google.common.base.Strings;

/**
 * Created by zhangbaoshan on 2016/5/25.
 */
public class PrintTable {

    private class Index {

        int _row, _colum;

        public Index (int r, int c) {
            _row= r;
            _colum= c;
        }

        @Override
        public boolean equals (Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Index other= (Index) obj;
            if (_colum != other._colum)
                return false;
            if (_row != other._row)
                return false;
            return true;
        }

        @Override
        public int hashCode () {
            final int prime= 31;
            int result= 1;
            result= prime * result + _colum;
            result= prime * result + _row;
            return result;
        }
    }

    Map<Index, String> _strings= new HashMap<Index, String>();
    Map<Integer, Integer> _columSizes= new HashMap<Integer, Integer>();

    int _numRows= 0;
    int _numColumns= 0;

    public int get_numRows(){
        return _numRows;
    }

    public void addString (int row, int colum, String content) {
        _numRows= Math.max(_numRows, row + 1);
        _numColumns= Math.max(_numColumns, colum + 1);

        Index index= new Index(row, colum);
        _strings.put(index, content);

        setMaxColumnSize(colum, content);
    }

    private void setMaxColumnSize (int colum, String content) {
        int size= content.length();
        Integer currentSize= _columSizes.get(colum);
        if (currentSize == null || (currentSize != null && currentSize < size)) {
            _columSizes.put(colum, size);
        }
    }

    public int getColumSize (int colum) {
        Integer size= _columSizes.get(colum);
        if (size == null) {
            return 0;
        } else {
            return size;
        }
    }

    public String getString (int row, int colum) {
        Index index= new Index(row, colum);
        String string= _strings.get(index);
        if (string == null) {
            return "";
        } else {
            return string;
        }
    }

    public String getTableAsString (int padding) {
        String out= "";
        for (int r= 0; r < _numRows; r++) {
            for (int c= 0; c < _numColumns; c++) {
                int columSize= getColumSize(c);
                String content= getString(r, c);
                int pad= c == _numColumns - 1 ? 0 : padding;
                out+= Strings.padEnd(content, columSize + pad, ' ');
            }

            //print out seprator
            if(r==0){
                //out+= "\n";
                out = insertSeperator(out);
                //out+= Strings.repeat("=",_numColumns*padding);
            }

            if (r < _numRows - 1) {
                out+= "\n";
            }


        }
        return out;
    }

    private String insertSeperator(String out) {
        out+= "\n";
        for (int c= 0; c < _numColumns; c++) {
            int columSize= getColumSize(c);
            //int pad= c == _numColumns - 1 ? 0 : padding;
            out+= Strings.repeat("=", columSize);
        }
        out+= Strings.repeat("=",_numColumns*padding);
        return out;
    }

    private int padding=2;

    @Override
    public String toString () {
        return getTableAsString(padding);
    }

}