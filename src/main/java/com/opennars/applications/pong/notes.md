== Old commits

== Current commit

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


== TODO
    * forgetting of very old SDR's in the SDR database - we can retain a few 100's to a few 1000's of SDR's easiliy

