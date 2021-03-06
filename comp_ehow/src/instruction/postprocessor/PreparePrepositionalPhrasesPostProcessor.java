/*
 * Copyright (c) 2009-10 Daniel Nyga
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Technische Universiteit Eindhoven nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
*/
package instruction.postprocessor;

import instruction.exceptions.InstructionException;
import instruction.semanticObjects.Instruction;
import instruction.semanticObjects.ObjectX;
import instruction.semanticObjects.Preposition;
import instruction.semanticObjects.SemanticObject;
import instruction.semanticObjects.Word;
import instruction.wordnet.PrepositionalMap;
import instruction.wordnet.WordNetRDF2;

import java.util.ArrayList;
import java.util.List;

public class PreparePrepositionalPhrasesPostProcessor implements PostProcessor {

	public void run(List<Instruction> instructions) {

		for (int i = 0; i < instructions.size(); i++) {
			try {
				correctPrepositionalPhrase(instructions.get(i));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

		}
	}

	/**
	 * Recursively traverses nested <code>SemanticObject</code>s to find
	 * composed prepositions (e.g. "next to")
	 * 
	 * @param so
	 * @throws Exception
	 */
	private void correctPrepositionalPhrase(SemanticObject so) throws Exception {

		// ==================================================================
		// Instructions
		// ==================================================================
		if (so instanceof Instruction) {
			Instruction in = (Instruction) so;

			// Objects
			for (int i = 0; i < in.getObjects().size(); i++)
				correctPrepositionalPhrase(in.getObjects().get(i));

			// Prepositions
			for (int i = 0; i < in.getPrepositions().size(); i++) {
				try {
					correctPrepositionalPhrase(in.getPrepositions().get(i));
				} catch (Exception e) {
					System.out.println(e.getMessage());
					in.getPrepositions().remove(i);
					i -= 1;
					continue;
				}
			}
		}

		// ==================================================================
		// Objects
		// ==================================================================
		else if (so instanceof ObjectX) {
			ObjectX o = (ObjectX) so;

			// Prepositions
			for (int i = 0; i < o.getPrepositions().size(); i++) {
				try {
					correctPrepositionalPhrase(o.getPrepositions().get(i));
				} catch (Exception e) {
					System.out.println(e.getMessage());
					o.getPrepositions().remove(i);
					i -= 1;
					continue;
				}
			}

		}

		// ==================================================================
		// Prepositions
		// ==================================================================
		else if (so instanceof Preposition) {
			Preposition pp = (Preposition) so;

			List<Word> oldPP = pp.getPrepositions();
			List<Word> newPP = getPrepositionalCompositions(oldPP);
			pp.setPrepositions(newPP);

			// Objects
			for (int i = 0; i < pp.getObjects().size(); i++)
				correctPrepositionalPhrase(pp.getObjects().get(i));
		}
	}

	/**
	 * Finds composed prepositions in a list of words
	 * 
	 * @param oldList
	 * @return list with prepositions
	 * @throws Exception
	 */
	private List<Word> getPrepositionalCompositions(List<Word> oldList)
			throws Exception {

		List<Word> newList = new ArrayList<Word>();

		for (int i = 0; i < oldList.size(); i++) {

			Word ppName = new Word(oldList.get(i).getType(), oldList.get(i)
					.getLabel());
			Word ppNameTmp = new Word(ppName.getType(), ppName.getLabel());
			int j = i + 1;
			while (j <= oldList.size()) {

				if (PrepositionalMap.get(ppNameTmp.getLabel()) != null
						|| WordNetRDF2.wordExistsAs(ppNameTmp.getLabel(),
								WordNetRDF2.convertPOS(ppNameTmp.getType()))) {

					ppName.setLabel(ppNameTmp.getLabel());
					if (j < oldList.size())
						ppNameTmp.setLabel(ppNameTmp.getLabel() + " "
								+ oldList.get(j++).getLabel());
					else {
						i = j - 1;
						break;
					}
				} else {
					if (j > i + 1)
						i = j - 2;
					else
						throw new InstructionException("Preposition "
								+ ppNameTmp.getLabel() + " is unknown");
					break;
				}

			}
			if (PrepositionalMap.get(ppNameTmp.getLabel()) != null)
				ppName.setType(Word.TYPE_PREPOSITION);
			newList.add(ppName);
		}

		return newList;
	}

}
