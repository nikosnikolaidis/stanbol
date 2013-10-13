/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.stanbol.rules.adapters.jena.atoms;

import java.util.ArrayList;
import java.util.List;

import org.apache.stanbol.rules.adapters.AbstractAdaptableAtom;
import org.apache.stanbol.rules.adapters.jena.HigherOrderClauseEntry;
import org.apache.stanbol.rules.adapters.jena.NodeClauseEntry;
import org.apache.stanbol.rules.base.api.RuleAtom;
import org.apache.stanbol.rules.base.api.RuleAtomCallExeption;
import org.apache.stanbol.rules.base.api.UnavailableRuleObjectException;
import org.apache.stanbol.rules.base.api.UnsupportedTypeForExportException;
import org.apache.stanbol.rules.manager.atoms.ExpressionAtom;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * 
 * It adapts a StartWithAtom to Jena functor that implements the start-with through a regex.
 * 
 * @author anuzzolese
 * 
 */
public class StartsWithAtom extends AbstractAdaptableAtom {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T adapt(RuleAtom ruleAtom) throws RuleAtomCallExeption,
                                         UnavailableRuleObjectException,
                                         UnsupportedTypeForExportException {

        Node arg1Node = null;
        Node arg2Node = null;

        org.apache.stanbol.rules.manager.atoms.EndsWithAtom tmp = (org.apache.stanbol.rules.manager.atoms.EndsWithAtom) ruleAtom;

        ExpressionAtom argument = tmp.getArgument();
        ExpressionAtom term = tmp.getTerm();

        ClauseEntry clauseEntry1 = adapter.adaptTo(argument, Rule.class);
        ClauseEntry clauseEntry2 = adapter.adaptTo(term, Rule.class);

        List<ClauseEntry> clauseEntries = new ArrayList<ClauseEntry>();

        if (clauseEntry1 instanceof HigherOrderClauseEntry) {
            arg1Node = ((HigherOrderClauseEntry) clauseEntry1).getBindableNode();

            clauseEntries.addAll(((HigherOrderClauseEntry) clauseEntry1).getClauseEntries());
        } else if (clauseEntry1 instanceof NodeClauseEntry) {
            arg1Node = ((NodeClauseEntry) clauseEntry1).getNode();
        } else {
            throw new org.apache.stanbol.rules.base.api.RuleAtomCallExeption(getClass());
        }

        if (clauseEntry2 instanceof HigherOrderClauseEntry) {
            arg2Node = ((HigherOrderClauseEntry) clauseEntry2).getBindableNode();

            clauseEntries.addAll(((HigherOrderClauseEntry) clauseEntry2).getClauseEntries());
        } else if (clauseEntry2 instanceof NodeClauseEntry) {
            Node tmpNode = ((NodeClauseEntry) clauseEntry2).getNode();
            LiteralLabel literalLabel = tmpNode.getLiteral();
            String lexicalForm = literalLabel.getLexicalForm();

            StringBuilder sb = new StringBuilder();
            sb.append("^");
            sb.append(lexicalForm);

            String regex = sb.toString();

            arg2Node = Node_RuleVariable.createLiteral(regex);
        } else {
            throw new org.apache.stanbol.rules.base.api.RuleAtomCallExeption(getClass());
        }

        java.util.List<Node> nodes = new ArrayList<Node>();

        nodes.add(arg1Node);
        nodes.add(arg2Node);

        return (T) new Functor("regex", nodes, new BuiltinRegistry());

    }

}
