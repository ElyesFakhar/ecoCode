/*
 * SonarQube PHP Custom Rules Example
 * Copyright (C) 2016-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package fr.greencodeinitiative.php.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.statement.*;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * functional description : please see HTML description file of this rule (resources directory)
 */
@Rule(
        key = AvoidMultipleIfElseStatementCheck.RULE_KEY,
        name = AvoidMultipleIfElseStatementCheck.ERROR_MESSAGE,
        description = AvoidMultipleIfElseStatementCheck.ERROR_MESSAGE,
        priority = Priority.MINOR,
        tags = {"eco-design", "ecocode", "performance"})
public class AvoidMultipleIfElseStatementCheck extends PHPSubscriptionCheck {

    public static final String RULE_KEY = "EC2";
    public static final String ERROR_MESSAGE = "Use a switch statement instead of multiple if-else if possible";
    public static final int INDEX_NOT_FOUND = -1;

    @Override
    public List<Kind> nodesToVisit() {
        return List.of(Kind.IF_STATEMENT, Kind.ELSEIF_CLAUSE, Kind.ELSE_CLAUSE);
    }

    @Override
    public void visitNode(Tree tree) {
        Tree parentNode = tree.getParent();
        if ( ! (parentNode instanceof BlockTree)) {
            return;
        }

        visitConditionalNodes(0, new VariablesPerLevelDataStructure());

//        checkIfStatementAtTheSameLevel(tree);
//        checkElseIfStatement(tree);
    }

    private void visitConditionalNodes(int level, VariablesPerLevelDataStructure parentDataMap) {

    }

    private void checkIfStatementAtTheSameLevel(Tree tree) {
        int countIfStatement = 0;

        Tree parentNode = tree.getParent();
        if (!(parentNode instanceof BlockTree)) {
            return;
        }

        // getting parent bloc to count if several IF at the same level
        BlockTree node = (BlockTree) parentNode;
        int sizeBody = node.statements().size();
        for(int i=0; i<sizeBody;++i){
            if (node.statements().get(i) instanceof IfStatementTree){
                ++countIfStatement;
            }
        }
        if (countIfStatement > 1){
            context().newIssue(this, tree, ERROR_MESSAGE);
        }
    }

    private void checkElseIfStatement(Tree tree) {
        String ifTree = tree.toString();
        String findStr = "elseif";
        int count = countMatches(ifTree, findStr);
        if (count >= 2) {
           context().newIssue(this, tree, ERROR_MESSAGE);
        }
    }

    public static int countMatches(String str, String sub) {
        if (isBlankString(str) || isBlankString(sub)) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != INDEX_NOT_FOUND) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    public static boolean isBlankString(String str) {
        return str == null || str.isBlank();
    }

    private class VariablesPerLevelDataStructure {
        /*
        Map<Integer, Map<String, Integer>> ==>
            - Key : index of Level (1 = first level)
            - Value : Map<String, Integer>
                - Key : name of variable in the current level
                - Value : number of usage of this variable in a IF statement in current level or one of parent levels
        */
        private final Map<Integer, Map<String, Integer>> mapVariablesPerLevel;

        public VariablesPerLevelDataStructure() {
            mapVariablesPerLevel = new HashMap<>(10);
        }

        public VariablesPerLevelDataStructure(Map<Integer, Map<String, Integer>> pMapVariablesPerLevel) {
            mapVariablesPerLevel = Map.copyOf(pMapVariablesPerLevel);
        }

        public void incrementUsageForVariableForLevel(String variableName, int level) {

            // variables map initilization if absent
            Map<String, Integer> mapVariables = mapVariablesPerLevel.computeIfAbsent(level, k -> new HashMap<>(5));

            Integer nbUsed = mapVariables.get(variableName);
            if (nbUsed == null) {
                nbUsed = 0;
            }
            nbUsed++;
            mapVariables.put(variableName, nbUsed);
        }
    }

}
