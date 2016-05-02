
/** <module> knowrob_sim_games

  Copyright (C) 2016 by Andrei Haidu

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
      * Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
      * Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
      * Neither the name of the <organization> nor the
        names of its contributors may be used to endorse or promote products
        derived from this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  @author Andrei Haidu
  @license BSD
*/

:- module(chem_queries,
    [
        c1_test/0,
        pour_test/0,
        pipette_test/0
    ]).


c1_test :-
	
	sg_load_experiments('/home/haidu/sr_experimental_data/chem_ex'),
	
	connect_to_db('CHEM-db'),
	
	% load the semantic map
	owl_parse('package://knowrob_sim_games/owl/chem_table.owl'),
	marker_update(object('http://knowrob.org/kb/chem_table.owl#SimChemMap_gVb3')),

   	exp_tag(EpInst, ExpTag),
	write('** Experiment tag: '), write(ExpTag), nl,

	% get events which occurred in the experiments
	sg_occurs(EpInst, GraspPipetteEventInst, GrStart, GrEnd),
	% check for grasping events
	event_type(GraspPipetteEventInst, knowrob:'GraspingSomething'),	
	% check object acted on
	acted_on(GraspPipetteEventInst, knowrob:'Pipette'),

	write('Start:'), writeln(GrStart), write('End:'), writeln(GrEnd).
	

pour_test :-	
    sg_load_experiments('/home/haidu/sr_experimental_data/chem_pour'),    
    connect_to_db('CHEM-db'),

    % get the instance of the current episode
    ep_inst(EpInst),

    % get events which occurred in the experiments
    sg_occurs(EpInst, EventInst, Start, End),
    % check for particle translation
    event_type(EventInst, knowrob_sim:'ParticleTranslation'),

    rdf_has(EventInst, knowrob:'fromLocation', ContInst),

    rdf_has(ContInst, rdf:type, ContType),
    not(rdf_equal(ContType, owl:'NamedIndividual')),

    rdf_has(EventInst, knowrob:'particleType', StuffInst),

    rdf_has(StuffInst, rdf:type, StuffType),
    not(rdf_equal(StuffType, owl:'NamedIndividual')),

    write('Start:'), writeln(Start),
    write('End:'), writeln(End),
    write('EventInst:'), writeln(EventInst),
    write('StuffInst:'), writeln(StuffInst),
    write('StuffType:'), writeln(StuffType),
    write('ContInst:'), writeln(ContInst),
    write('ContType:'), writeln(ContType).


%% % check objects in contact
%% in_contact(EventInst, O1Type, O2Type) :-
%%     % get the objects in contact
%%     rdf_has(EventInst, knowrob_sim:'inContact', O1Inst),
%%     rdf_has(EventInst, knowrob_sim:'inContact', O2Inst),    
%%     % make sure the objects are distinct
%%     O1Inst \== O2Inst,
%%     % check for the right type of contacts    
%%     rdf_has(O1Inst, rdf:type, O1Type),
%%     rdf_has(O2Inst, rdf:type, O2Type),
%%     % avoid NamedIndividual returns for the object types
%%     not(rdf_equal(O1Type, owl:'NamedIndividual')),
%%     not(rdf_equal(O2Type, owl:'NamedIndividual')).


pipette_test :-    
    sg_load_experiments('/home/haidu/sr_experimental_data/chem_pipette'),    
    connect_to_db('CHEM-db').