/********************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.traceability.policies.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.traceability.assets.domain.importpoc.exception.PolicyNotFoundException;
import org.eclipse.tractusx.traceability.notification.domain.contract.EdcNotificationContractService;
import org.eclipse.tractusx.traceability.policies.application.service.PolicyService;
import org.springframework.stereotype.Service;
import policies.request.RegisterPolicyRequest;
import policies.request.UpdatePolicyRequest;
import policies.response.CreatePolicyResponse;
import policies.response.IrsPolicyResponse;
import policies.response.PolicyResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static policies.response.IrsPolicyResponse.toResponse;

@Slf4j
@RequiredArgsConstructor
@Service
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final EdcNotificationContractService edcNotificationContractService;

    @Override
    public Map<String, List<IrsPolicyResponse>> getIrsPolicies() {
        return policyRepository.getPolicies();
    }

    @Override
    public List<PolicyResponse> getPolicies() {
        Map<String, List<IrsPolicyResponse>> policies = policyRepository.getPolicies();
        return toResponse(policies);
    }

    @Override
    public PolicyResponse getPolicy(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Policy ID must not be null or empty");
        }

        Map<String, Optional<IrsPolicyResponse>> policies = policyRepository.getPolicy(id);

        return policies.entrySet().stream()
                .filter(entry -> entry.getValue().isPresent())
                .findFirst()
                .map(entry -> toResponse(entry.getValue().get(), entry.getKey()))
                .orElseThrow(() -> new PolicyNotFoundException("Policy with id: %s not found.".formatted(id)));
    }

    @Override
    public CreatePolicyResponse createPolicy(RegisterPolicyRequest registerPolicyRequest) {
        if (registerPolicyRequest == null) {
            throw new IllegalArgumentException("RegisterPolicyRequest must not be null");
        }
        CreatePolicyResponse policy = policyRepository.createPolicy(registerPolicyRequest);
        edcNotificationContractService.updateNotificationContractDefinitions();
        return policy;
    }

    @Override
    public void deletePolicy(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Policy ID must not be null or empty");
        }
        policyRepository.deletePolicy(id);
        edcNotificationContractService.updateNotificationContractDefinitions();
    }

    @Override
    public void updatePolicy(UpdatePolicyRequest updatePolicyRequest) {
        if (updatePolicyRequest == null) {
            throw new IllegalArgumentException("UpdatePolicyRequest must not be null");
        }
        policyRepository.updatePolicy(updatePolicyRequest);
        edcNotificationContractService.updateNotificationContractDefinitions();
    }
}
