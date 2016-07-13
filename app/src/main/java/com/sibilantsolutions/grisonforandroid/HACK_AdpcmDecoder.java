package com.sibilantsolutions.grisonforandroid;

//TODO: Check out javax.sound.sampled.spi.FormatConversionProvider
public class HACK_AdpcmDecoder {
    final static /*package*/ int index_adjust[] = {-1, -1, -1, -1, 2, 4, 6, 8};
    //-1,-1,-1,-1,2,4,6,8};  Only needed for IMA impl.

    final static /*package*/ int step_table[] = //89 values
            {
                    7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
                    50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371,
                    408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
                    2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493,
                    10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
            };

    private int predictedSample = 0;
    private int index = 0;
    //private int stepSize = 7;     //Only for IMA impl.

    public byte[] decode(byte[] raw) {
        return decode(raw, 0, raw.length);
    }

    /**
     * Decode 4-bit ADPCM samples into 16-bit linear PCM data (signed, big endian).
     *
     * @param raw
     * @param offset
     * @param len
     * @return
     */
    public byte[] decode(byte[] raw, int offset, int len) {
        return decodeFos(raw, offset, len);
//        return decodeIma( raw, offset, len );
    }

    /*in*/
//This is ported from Foscam's ipcam_sample/ipcam.cpp
    private byte[] decodeFos(byte[] raw, int offset, int len) {
        //Each nibble (4 bits) turns into 2 bytes (16 bits);
        //each byte (8 bits) becomes 4 bytes (32 bits).
        byte[] ret = new byte[len * 4];

        //Use 2x length because we examine each byte twice.
        for (int i = offset; i < (offset + len) * 2; i++) {
            int nibble; //4 bits

            if (i % 2 != 0)   //odd i
            {
                nibble = raw[i / 2] & 0x0F;   //bottom four bits
            } else                //even i
            {
                nibble = raw[i / 2] >> 4;     //top four bits
            }

            boolean isSignBit = false;

            if ((nibble & 8) != 0)  //b0000 1000    top bit of nibble
                isSignBit = true;

            nibble &= 7;    //b0000 0111     bottom three bits

            int delta = (step_table[index] * nibble) / 4 + step_table[index] / 8;

            if (isSignBit)
                delta *= -1;

            predictedSample += delta;

            if (predictedSample > 32767)
                predictedSample = 32767;
            else if (predictedSample < -32768)
                predictedSample = -32768;

            //This is big endian.
            ret[i * 2] = (byte) (predictedSample >> 8);    //top 8 bits
            ret[i * 2 + 1] = (byte) (predictedSample & 0xFF);  //bottom 8 bits

            //At this point, only the nibble's bottom three bits are used so values can only be 0-7.
            index += index_adjust[nibble];

            if (index < 0)
                index = 0;
            else if (index > 88)  //Size of step_table
                index = 88;
        }

        return ret;
    }
//*/
    /**out/
     //This is from IMA_ADPCM.pdf from http://www.cs.columbia.edu/~hgs/audio/dvi.
     //It starts off the same as the Foscam impl but then the values start differing
     //by one for each 4-bit sample (2 byte resulting data).
     private byte[] decodeIma( byte[] raw, int offset, int len )
     {
     byte[] ret = new byte[len * 4];

     //Use 2x length because we examine each byte twice.
     for ( int i = offset; i < ( offset + len ) * 2; i++ )
     {
     int originalSample; //4 bits

     if ( i % 2 != 0 )   //odd i
     {
     originalSample = ( raw[i / 2] & 0xFF ) & 0x0F;   //bottom four bits
     }
     else                //even i
     {
     originalSample = ( raw[i / 2] & 0xFF ) >> 4;     //top four bits
     }

     int difference = 0;

     if ( ( originalSample & 4 ) != 0 )  //b0000 0100
     {
     difference += stepSize;
     }
     if ( ( originalSample & 2 ) != 0 )  //b0000 0010
     {
     difference += stepSize >> 1;
     }
     if ( ( originalSample & 1 ) != 0 )  //b0000 0001
     {
     difference += stepSize >> 2;
     }

     difference += stepSize >> 3;

     if ( ( originalSample & 8 ) != 0 )  //b0000 1000
     {
     difference = -difference;
     }

     predictedSample += difference;

     if ( predictedSample > 32767 )
     predictedSample = 32767;
     else if ( predictedSample < -32768 )
     predictedSample = -32768;

     //This is big endian.
     ret[i * 2]     = (byte)( ( predictedSample & 0xFF00 ) >> 8 );    //top 8 bits
     ret[i * 2 + 1] = (byte)( predictedSample & 0xFF );  //bottom 8 bits

     index += index_adjust[originalSample];
     if ( index < 0 )
     index = 0;
     else if ( index > 88 )  //Size of step_table
     index = 88;
     stepSize = step_table[index];
     }

     return ret;
     }
     //*/

}
