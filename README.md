# Flux OCI Sample

This repository demonstrates how to use Flux CD with OCI artifacts stored in GitHub Container Registry (GHCR) for Kubernetes deployments.

## Overview

This project showcases:

1. How to package Kubernetes manifests as OCI artifacts
2. How to configure Flux CD to deploy from OCI repositories
3. How to implement a GitOps workflow using Flux CD and OCI artifacts

## Prerequisites

- Kubernetes cluster with Flux CD installed
- GitHub account with permissions to push to GHCR

## Kubernetes Deployment with Flux and Kustomize

### How it works with Flux CD

[Flux CD](https://fluxcd.io/) is a GitOps operator for Kubernetes that ensures the state of your cluster matches the configuration stored in Git. This project is designed to work with Flux's OCI repository feature, which allows Flux to pull Kubernetes manifests from OCI registries like GHCR.

### Project Structure

This project focuses on using Flux CD with OCI artifacts. Below are the key directories related to Flux and its dependent configuration files:

#### Key Directories

##### .github/workflows Directory

Contains GitHub Actions workflow related to Flux:

- `deploy-oci.yaml`: Workflow that packages Kubernetes manifests as an OCI artifact and pushes to GHCR

##### k8s Directory

Contains the Kubernetes manifests that are used by Flux:

- `kustomization.yaml`: A Kustomize configuration that includes the deployment and service resources
- `deployment.yaml`: Defines the Kubernetes Deployment with resource limits and health probes
- `service.yaml`: Defines the Kubernetes Service to expose the application

These manifests are packaged as an OCI artifact and pushed to GHCR during the CI/CD process.

##### flux-example Directory

Contains example Flux resources that you would apply to your Kubernetes cluster to set up the deployment:

- `ocirepository.yaml`: Defines an OCIRepository resource that points to the OCI artifact in GHCR
- `kustomization.yaml`: Defines a Flux Kustomization resource that applies the manifests from the OCIRepository

### CI/CD Workflow for Flux

This project includes a GitHub Actions workflow that packages Kubernetes manifests as OCI artifacts for use with Flux CD:

- `deploy-oci.yaml`: Packages the Kubernetes manifests as an OCI artifact and pushes it to GHCR

When a new version of the application is pushed to the main branch:

1. The workflow updates the image tag in the deployment.yaml file
2. It packages the manifests and pushes them as an OCI artifact to GHCR with the tag "kustomize-latest"

### Kubernetes Manifests

The actual Kubernetes manifests in the `k8s` directory are:

```yaml
# k8s/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
  - deployment.yaml
  - service.yaml
```

```yaml
# k8s/deployment.yaml (simplified)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flux-oci-sample
  namespace: gne-test
spec:
  replicas: 1
  selector:
    matchLabels:
      app: flux-oci-sample
  template:
    metadata:
      labels:
        app: flux-oci-sample
    spec:
      containers:
      - name: flux-oci-sample
        image: ghcr.io/shinhancard/flux-oci-sample:image-REPLACEME
        ports:
        - containerPort: 8080
        # Resource limits and health probes are defined in the actual file
```

```yaml
# k8s/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: flux-oci-sample
  namespace: gne-test
spec:
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: flux-oci-sample
```

### Flux Configuration

The example Flux resources in the `flux-example` directory are:

```yaml
# flux-example/ocirepository.yaml
apiVersion: source.toolkit.fluxcd.io/v1beta2
kind: OCIRepository
metadata:
  name: flux-oci-sample
  namespace: gne-test
spec:
  interval: 1m
  url: oci://ghcr.io/shinhancard/flux-oci-sample
  ref:
    tag: kustomize-latest
```

```yaml
# flux-example/kustomization.yaml
apiVersion: kustomize.toolkit.fluxcd.io/v1
kind: Kustomization
metadata:
  name: flux-oci-sample-app
  namespace: gne-test
spec:
  interval: 10m0s
  path: ./k8s
  prune: true
  sourceRef:
    kind: OCIRepository
    name: flux-oci-sample
  targetNamespace: gne-test
  images:
  - name: ghcr.io/shinhancard/flux-oci-sample
    newName: ghcr.io/shinhancard/flux-oci-sample
```

## Flux Deployment Process

The Flux deployment process works as follows:

1. The `deploy-oci.yaml` workflow packages Kubernetes manifests as an OCI artifact and pushes it to GHCR with the tag "kustomize-latest"
2. Flux detects the new OCI artifact in GHCR based on the OCIRepository configuration
3. Flux applies the updated manifests to the cluster according to the Kustomization resource
4. Kubernetes pulls the new container image and updates the running pods

This creates a GitOps-based deployment pipeline using Flux CD and OCI artifacts.

## License

This project is licensed under the terms of the license included in the repository.
