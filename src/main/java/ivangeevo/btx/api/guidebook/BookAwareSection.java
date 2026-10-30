package ivangeevo.btx.api.guidebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** A section that needs to know which book it was added to, e.g. an index listing that book's sections. */
@Environment(EnvType.CLIENT)
public interface BookAwareSection {
	/** Called once, right after the book's sections are filled. */
	void onAddedToBook(GuidebookBook book);
}