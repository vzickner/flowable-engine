/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flowable.cmmn.test.migration;

import org.flowable.cmmn.api.migration.PlanItemMigrationMapping;
import org.flowable.cmmn.api.repository.CaseDefinition;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.api.runtime.PlanItemInstance;
import org.flowable.cmmn.api.runtime.PlanItemInstanceState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Valentin Zickner
 */
public class CaseInstanceMigrationTest extends AbstractCaseMigrationTest {

    @Test
    void withSimpleOneTaskCase() {
        // Arrange
        deployCaseDefinition("test1", "org/flowable/cmmn/test/migration/one-task.cmmn.xml");
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder().caseDefinitionKey("testCase").start();
        CaseDefinition destinationDefinition = deployCaseDefinition("test1", "org/flowable/cmmn/test/migration/two-task.cmmn.xml");

        // Act
        cmmnMigrationService.createCaseInstanceMigrationBuilder()
                .migrateToCaseDefinition(destinationDefinition.getId())
                .migrate(caseInstance.getId());

        // Assert
        CaseInstance caseInstanceAfterMigration = cmmnRuntimeService.createCaseInstanceQuery()
                .caseInstanceId(caseInstance.getId())
                .singleResult();
        assertThat(caseInstanceAfterMigration.getCaseDefinitionId()).isEqualTo(destinationDefinition.getId());
        List<PlanItemInstance> planItemInstances = cmmnRuntimeService.createPlanItemInstanceQuery()
                .caseInstanceId(caseInstance.getId())
                .list();
        assertThat(planItemInstances).hasSize(2);
        assertThat(planItemInstances)
                .extracting(PlanItemInstance::getCaseDefinitionId)
                .containsOnly(destinationDefinition.getId());
        assertThat(planItemInstances)
                .extracting(PlanItemInstance::getName)
                .containsExactlyInAnyOrder("Task 1", "Task 2");
        assertThat(planItemInstances)
                .extracting(PlanItemInstance::getState)
                .containsOnly(PlanItemInstanceState.ACTIVE);
    }

    @Test
    void withSimpleOneTaskCaseWithMappingToSecondNewTask() {
        // Arrange
        deployCaseDefinition("test1", "org/flowable/cmmn/test/migration/one-task.cmmn.xml");
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder().caseDefinitionKey("testCase").start();
        CaseDefinition destinationDefinition = deployCaseDefinition("test1", "org/flowable/cmmn/test/migration/two-task.cmmn.xml");

        // Act
        cmmnMigrationService.createCaseInstanceMigrationBuilder()
                .migrateToCaseDefinition(destinationDefinition.getId())
                .addActivityMigrationMapping(new PlanItemMigrationMapping.OneToOneMapping("humanTask1", "humanTask2"))
                .migrate(caseInstance.getId());

        // Assert
        CaseInstance caseInstanceAfterMigration = cmmnRuntimeService.createCaseInstanceQuery()
                .caseInstanceId(caseInstance.getId())
                .singleResult();
        assertThat(caseInstanceAfterMigration.getCaseDefinitionId()).isEqualTo(destinationDefinition.getId());
        List<PlanItemInstance> planItemInstances = cmmnRuntimeService.createPlanItemInstanceQuery()
                .caseInstanceId(caseInstance.getId())
                .list();
        assertThat(planItemInstances).hasSize(2);
        assertThat(planItemInstances)
                .extracting(PlanItemInstance::getCaseDefinitionId)
                .containsOnly(destinationDefinition.getId());
        assertThat(planItemInstances)
                .extracting(PlanItemInstance::getName)
                .containsExactlyInAnyOrder("Task 1", "Task 2");
        assertThat(planItemInstances)
                .extracting(PlanItemInstance::getState)
                .containsOnly(PlanItemInstanceState.ACTIVE);
    }

}