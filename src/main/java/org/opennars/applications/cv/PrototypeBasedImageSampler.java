/*
 * The MIT License
 *
 * Copyright 2019 Robert Wünsche <rt09@protonmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.applications.cv;

import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// samples a image and stores the prototypes
public class PrototypeBasedImageSampler {
    public UlProtoClassifier classifier = new UlProtoClassifier();

    public Map2d heatmap = null; // heatmap used for stochastic sampling
    public int heatmapCellsize = 32;

    public Random rng = new Random();

    public int numberOfSamples = 0;

    public List<Classification> sample(PImage img) {
        Map2d grayscaleImage = new Map2d(img.height, img.width);
        for(int iy=0;iy<img.height;iy++) {
            for(int ix=0;ix<img.width;ix++) {

                int colorcode =  img.pixels[iy*img.width+ix];
                //TODO check if the rgb is extracted correctly
                float r = (colorcode & 0xff) / 255.0f;
                float g = ((colorcode >> 8) & 0xFF) / 255.0f;
                float b = ((colorcode >> 8*2) & 0xFF) / 255.0f;

                float grayscale = (r+g+b)/3.0f;

                grayscaleImage.writeAtUnsafe(iy, ix, grayscale);
            }
        }


        List<Classification> classifications = new ArrayList<>();

        float integralOfHeatmap = 0;
        if (heatmap != null) {
            integralOfHeatmap = calcIntegralOfHeatmap();
        }


        for(int iSample=0;iSample<numberOfSamples;iSample++) {
            int posX = 0, posY = 0;

            if (heatmap != null) { // sample by heatmap
                float chosenIntgrlVal = rng.nextFloat() * integralOfHeatmap;

                float currentIntrl = 0;
                boolean intrlDone = false;
                for(int iy=0;iy<heatmap.retHeight();iy++) {
                    for(int ix=0;ix<heatmap.retWidth();ix++) {
                        currentIntrl += heatmap.readAtSafe(iy,ix);

                        if(currentIntrl >= chosenIntgrlVal) { // integration finished, we found the position of this sample
                            posX = ix * heatmapCellsize;
                            posY = iy * heatmapCellsize;

                            intrlDone = true;
                            break;
                        }
                    }
                    if(intrlDone) {
                        break;
                    }
                }
            }
            else { // sample uniformly
                posX = rng.nextInt(img.width);
                posY = rng.nextInt(img.height);
            }

            int prototypeSize = 16; // size of the prototype
            int stride = 4;
            float[] convResult = Conv.convAt(grayscaleImage, posX, posY, prototypeSize, stride);

            long classification = classifier.classify(convResult);

            System.out.println("[d ] classification = " + Long.toString(classification));

            int here = 0;

            // put classification into result together with the coordinate
            Classification cl = new Classification();
            cl.posX = posX;
            cl.posY = posY;
            cl.class_ = classification;
            classifications.add(cl);
        }

        return classifications;
    }

    private float calcIntegralOfHeatmap() {
        float integral = 0;
        for(int iy=0;iy<heatmap.retHeight();iy++) {
            for(int ix=0;ix<heatmap.retWidth();ix++) {
                integral += heatmap.readAtSafe(iy, ix);
            }
        }
        return integral;
    }

    public static class Classification {
        public int posX, posY;
        public long class_;
    }
}
