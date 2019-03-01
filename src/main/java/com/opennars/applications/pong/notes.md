## Old commits

### https://github.com/opennars/opennars-applications/commit/44a439671a26a1ae8a1df4682e3f3611971451aa

Problems of tracking and recognizing objects from pixel data are the following
 * identification of new unknown objects
 * recognition of known objects - even if their shapes don't match up
 * ability for the AI to inspect all representations down to the pixel level if necessary

Requirement: Everything has to work in realtime and in a unsupervised online fashion.

A good idea might seem to handle objects as a collection of lower level components.
I call these lower level components on pixel level "patches".
A patch is just a rectangle of the screen.

These patches can be matched to each other with a score from 0.0 (no match) to 1.0 (full match).
SDR's are a good representation here because
* no need for translational invariance - because the patches are small and the translation invariance is handled on a higher level
* comparision is extremely cheap


Experiment:
+ tracing of patches is working (it is tracking the ball)
- need to forget old patches which were not moving
  ? how to handle objects which stopped moving but are still there
    An idea to solve this is that sampling may be used to reidentify a moved objects - because a moved object changes the screen at that position
    We can store the old patch for that sampled position and compare it with the new patch if the position is resampled
       The attention system can get informed when a change was detected.

## https://github.com/opennars/opennars-applications/commit/f4f0406138959237ed0c88e21f46137381078163

* drawing of ball
* drawing of bat

* attention : don't forget tracked patched if they were moving

we need a attention system which looks at the difference of the pixels - so we can (re)idenfiy either to fast moving objects or "teleporting" objects
+ this works very well
- leads to a lot of tracked patches because they get "collected"

## commit

Added Proto-Objects
def: Proto-objects are "clusters" (of patches) which show a similar behaviour, shape, color, etc.
Can be used by higher level reasoning processes to compose objects out of proto-objects.


## commit

* solved problem of left off proto-objects
* implemented merging of proto-objects by age - we throw the older proto-object away

## commit

* giving NARS the position informations from the (sorted) proto-objects
- produces to complex products for to many objects (>= 4) - and it slows down

## commit

* give NARS just the first 2-ary product
* tuning for patches -> protoobject translation

Result:
+ ball is tracked fine
- translation based on patches has problem with recognizing the bat as an proto-object at all


- protoobjects don't match up with the bat most of the time
  algorithm of the patches is probably to complex and I should resort to something much simpler - I have an idea

## commit

* tried patricks encoding - which is just to put the active pixels as events
  performance is still not good with 3.0.1

It had a good run but then went onto the ^down spree
https://www.youtube.com/watch?v=ZZfh5voSdL4

## commit

* trying patricks encoing again with labeled pixels (by x position)
* tuned forced random chance - old value seems to high

Result:
~ after >~30 mins - agent pressed a long time ^down (as usual) - and then recovered
  after >~45 mins - agent is in a predador like state in the bottom corner and can react to ball in other positions of the screen

## commit

* begun "retina" approach
  key idea is that the agent can choose to focus on objects
  relative positions (to other objects) are put into the NAR as events

Result:
Plays well after a short time (<~10 min)

## commit

* experimented with op movement delta of 3 pixels - got stuck in corner - might be due to bad luck
* experimented with op movement delta of 5 pixels - got stuck in corner

* current tuning
-> leads to result of ~50% - random hit chance is ~10%
[i] #balls=523 pseudoscore=238.0 t=138600

## commit

* experimented with switchable axis

result (same run):
[i] #balls=347 pseudoscore=82.0 t=72600
[i] #balls=467 pseudoscore=108.0 t=97200

Agent doesn't learn it at all to switch to the y axis.

## commit

* tried again seperated axis of relatie ball position
- didn't work - maybe decision making has an issue
  it seemed to learn to associate ^down with certain x values - this led the agent to continiously miss the ball


* tried x,y tuple of position
[i] #balls=690 pseudoscore=320.0 t=184200
-> not so good

## commit

* relative position
* modified ^selectAxis to give the relatie position into NARS immediatly
* another experiment in the direction of "active vision" 

result
[i] #balls=492 pseudoscore=140.0 t=109200
-> not good at all

## commit

* changed representation to 2d product with bias to y axis

result
[i] #balls=696 pseudoscore=342.0 t=190800

## commit

* representation is fed from proto-objects
* using unlabeled event representation
  ex: <{y10x5, y1x3}-->[V]>

[i] #balls=724 pseudoscore=307.0 t=186000
-> not so good

## commit

* representation is fed from game-objects
* using unlabeled event representation
  ex: <{y10x5, y1x3}-->[V]>

[i] #balls=632 pseudoscore=288.0 t=167400
-> not so good

## commit

* representation is fed from game-objects
* using labeled event representation

[i] #balls=578 pseudoscore=280.0 t=157200
-> not so good

## commit

Experiment:

* representation is fed from game-objects - absolute position - only y axis
* using unlabeled event representation

[i] #balls=321 pseudoscore=97.0 t=72600
-> not so good

Experiment:

* representation is fed from game-objects - absolute position - only y axis
* using labeled event representation
  ex
  String narsese = "<y" + (int)(batEntity.posY / 10) + " --> [batY]>. :|:";
  String narsese = "<y" + (int)(ballEntity.posY / 10) + " --> [ballY]>. :|:";

[i] #balls=12 pseudoscore=30.0 t=9600
-> very good

## commit

Experiment:


* representation is fed from game-objects - absolute position - only y axis
* using labeled event representation
  ex
  String narsese = "<{y" + (int)(batEntity.posY / 10) + "} --> [batY]>. :|:";
  String narsese = "<{y" + (int)(ballEntity.posY / 10) + "} --> [ballY]>. :|:";


[i] #balls=16 pseudoscore=35.0 t=11400
-> very good

Conclusion: the set is not the reason for the poor performance for the unlabeled case

## commit

Experiment:

* representation is fed from game-objects - absolute position - only y axis
* using labeled event representation with one unlabeled predicate
  ex
  final String str = "0y" + (int)(ballEntity.posY / 10.0) + "x" + (int)(ballEntity.posX / 1000);
  final String str = "1y" + (int)(batEntity.posY / 10.0) + "x" + (int)(batEntity.posX / 1000);

## commit

* changed pong environment

Experiment: to compute the baseline of a agent which doesn't do anything
[i] #balls=1100 pseudoscore=430.0 t=271800

## commit

* changed representation to product with some y resolution

experiment with 80k concepts and 200 term and tasklinks
[i] #balls=950 pseudoscore=782.0 t=338400

## commit

* full resolution
* movement is direction based - op's only set the direction and movement is automatic

experiment:
#balls=1316 pseudoscore=814.0 t=401400


## commit

added Temporal Q&A

experiment - with temporal Q&A
    x resolution grid is 20
    
    [i] #balls=753 pseudoscore=665.0 t=280200

experiment - without temporal Q&A
    x resolution grid is 20
    
    [i] #balls=753 pseudoscore=462.0 t=228600

## current commit

experiment - with temporal Q&A

    x resolution grid is 20
    #concepts is 120k

[i] #balls=1193 pseudoscore=775.0 t=373800

== TODO

    * retina - agent has to be able to select object of focus - positions of other objects will be relative to it
    
    * forgetting of very old SDR's in the SDR database - we can retain a few 100's to a few 1000's of SDR's easiliy