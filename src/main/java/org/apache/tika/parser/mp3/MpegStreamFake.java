/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.tika.parser.mp3;

import java.io.InputStream;

/**
 *
 * @author avn
 */
public class MpegStreamFake extends MpegStream {
    
    public MpegStreamFake(InputStream in) {
        super(in);
    }
    
}
