# Tick Persistence Change: Regression Checklist

## Before you start
- [ ] Commit the change on its own in git
- [ ] Back up the test world

## The original bug (should be fixed)
- [X] Crank it, quit immediately, relog: the crank finishes and the millstone stops
- [ ] Crank is not stuck, and can be cranked again
- [ ] Nothing is permanently powered after a relog

## Blocks that "do something over time"
- [ ] Water and lava: quit mid-flow, relog, and they finish spreading normally
- [ ] Sand and gravel: quit while one is about to fall
- [ ] Redstone torches and clocks: a running one keeps running, with no flicker
- [ ] Other mechanical blocks (millstone, gearbox, axles)
- [ ] Fire, crops, saplings, leaf decay

## Worlds and saving
- [ ] An old world (saved before the change) loads without errors
- [ ] A brand-new world works normally
- [ ] Quit via the menu, then also test autosave
- [ ] Fly far away so chunks unload, come back, and the crank is still fine
- [ ] A crank on a chunk edge, and in negative coordinates

## Performance
- [ ] Saving doesn't stutter, even with a big water or redstone build
- [ ] No lag spikes during normal play

## The log
- [ ] No errors or crashes on load or quit
- [ ] No "out of sync" messages
- [ ] Remove the debug `println` lines when you're done

## Known and expected (not caused by this change)
- [ ] Repeater clocks freeze after you leave their chunks and come back (also happens without the mixins; vanilla b1.7.3/BTA behavior)
