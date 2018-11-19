/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
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
package com.opennars.applications.crossing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;
import org.opennars.entity.TruthValue;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;
import org.opennars.io.events.EventEmitter;
import org.opennars.io.events.Events;
import org.opennars.io.events.OutputHandler;
import org.opennars.language.Inheritance;
import org.opennars.language.Product;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.storage.Memory;

public class NarListener implements EventEmitter.EventObserver {  
    public class Prediction
    {
        public Entity ent;
        public long time;
        public TruthValue truth;
        public Prediction(Entity ent, TruthValue truth, long time) {
            this.ent = ent;
            this.time = time;
            this.truth = truth;
        }
    }

    Map<Vec2Int, List<Prediction>> predictionsByMapPosition;
    List<NarListener.Prediction> disappointments;
    Nar nar;
    Camera camera;
    public NarListener(Camera camera, Nar nar, Map<Vec2Int, List<Prediction>> predictionsByMapPosition, List<NarListener.Prediction> disappointments) {
        this.predictionsByMapPosition = predictionsByMapPosition;
        this.disappointments = disappointments;
        this.nar = nar;
        this.camera = camera;
    }
    Term pedestrian = Term.get("pedestrian");
    Term car = Term.get("car");
    Term at = Term.get("at");
    @Override
    public void event(Class event, Object[] args) {
        if(event == OutputHandler.DISAPPOINT.class) {
            Term term = (Term) args[0];
            Sentence s = new Sentence(term, Symbols.JUDGMENT_MARK, new TruthValue(0.0f,0.9f, nar.narParameters), new Stamp(nar, nar.memory));
            Task t = new Task(s, null, Task.EnumType.DERIVED);
            Prediction result = predictionFromTask(t);
            if(result != null) {
                int showFor = 100; //how long the disappointment should be displayed in the GUI
                result.time = nar.time() + showFor;
                disappointments.add(result);
            }
        }
        if (event == Events.TaskAdd.class) {
            Task t = (Task) args[0];
            if (/*t.sentence.getOccurenceTime() > nar.time() && */t.sentence.isJudgment() && t.sentence.getTruth().getExpectation() >= nar.narParameters.DEFAULT_CONFIRMATION_EXPECTATION) {
                predictionFromTaskAndAddToPredictions(t);
            }
        }
    }

    public void predictionFromTaskAndAddToPredictions(Task t) {
        Prediction prediction = null;
        int posX=0, posY=0;

        //format: "<(*,car,50_82) --> at>. %0.45;0.26%";
        if(t.sentence.term instanceof Inheritance) {
            Inheritance positionInh = (Inheritance) t.sentence.term;
            if(positionInh.getSubject() instanceof Product) {
                Product prod = (Product) positionInh.getSubject();
                if(prod.size() == 2) {
                    Term type = prod.term[0];
                    String position = prod.term[1].toString();
                    if(position.contains("_")) {
                        try {
                            posX = camera.minX + Util.discretization * Integer.valueOf(position.split("_")[0]);
                            posY = camera.minY + Util.discretization * Integer.valueOf(position.split("_")[1]);
                            //int id = 0; //Integer.valueOf(idStr.toString()); often a dep var
                            Entity pred;
                            if(type.toString().startsWith(car.toString())) {
                                String id = type.toString().substring(car.toString().length(), type.toString().length());
                                pred = new Car(Integer.valueOf(id), posX, posY, 0, 0);
                                prediction = new Prediction(pred, t.sentence.truth, t.sentence.getOccurenceTime());
                            }
                            else
                            if(type.toString().startsWith(pedestrian.toString())) {
                                String id = type.toString().substring(pedestrian.toString().length(), type.toString().length());
                                pred = new Pedestrian(Integer.valueOf(id), posX, posY, 0, 0);
                                prediction = new Prediction(pred, t.sentence.truth, t.sentence.getOccurenceTime());
                            }
                        } catch(Exception ex) {} //wrong format, it's not such a type of prediction but something else
                    }
                }
            }
        }

        if (prediction == null) {
            return;
        }

        if (!predictionsByMapPosition.containsKey(new Vec2Int(posX, posY))) {
            predictionsByMapPosition.put(new Vec2Int(posX, posY), new ArrayList<>());
        }
        List<Prediction> predictions = predictionsByMapPosition.get(new Vec2Int(posX, posY));
        predictions.add(prediction);
    }

    public Prediction predictionFromTask(Task t) {
        Prediction prediction = null;
        //format: "<(*,car,50_82) --> at>. %0.45;0.26%";
        if(t.sentence.term instanceof Inheritance) {
            Inheritance positionInh = (Inheritance) t.sentence.term;
            if(positionInh.getSubject() instanceof Product) {
                Product prod = (Product) positionInh.getSubject();
                if(prod.size() == 2) {
                    Term type = prod.term[0];
                    String position = prod.term[1].toString();
                    if(position.contains("_")) {
                        try {
                            int posX = camera.minX + Util.discretization * Integer.valueOf(position.split("_")[0]);
                            int posY = camera.minY + Util.discretization * Integer.valueOf(position.split("_")[1]);
                            //int id = 0; //Integer.valueOf(idStr.toString()); often a dep var
                            Entity pred;
                            if(type.toString().startsWith(car.toString())) {
                                String id = type.toString().substring(car.toString().length(), type.toString().length());
                                pred = new Car(Integer.valueOf(id), posX, posY, 0, 0);
                                prediction = new Prediction(pred, t.sentence.truth, t.sentence.getOccurenceTime());
                            }
                            else  
                            if(type.toString().startsWith(pedestrian.toString())) {
                                String id = type.toString().substring(pedestrian.toString().length(), type.toString().length());
                                pred = new Pedestrian(Integer.valueOf(id), posX, posY, 0, 0);
                                prediction = new Prediction(pred, t.sentence.truth, t.sentence.getOccurenceTime());
                            }
                        } catch(Exception ex) {} //wrong format, it's not such a type of prediction but something else
                    }
                }
            }
        }
        return prediction;
    }
}