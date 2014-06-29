/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba;

import io.netty.buffer.ByteBuf;

/**
 *
 * @author atul
 */
public interface Message {
    public ByteBuf result();
}
