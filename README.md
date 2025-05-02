# Flux OCI Sample

This repository contains a Spring Boot application that demonstrates how to build and deploy OCI images to GitHub Container Registry (GHCR) and use them with Flux CD and Kustomize in a Kubernetes environment.

## Project Overview

This is a simple Spring Boot application that provides a REST API endpoint returning "hello spring". The project is configured to:

1. Build the application using Gradle
2. Package it as an OCI image using Jib
3. Push the image to GitHub Container Registry (GHCR)
4. Deploy to Kubernetes using Flux CD and Kustomize

## Prerequisites

- JDK 21
- Gradle
- Docker (for local testing)
- Kubernetes cluster with Flux CD installed
- GitHub account with permissions to push to GHCR

## Building and Running Locally

### Clone the repository

```bash
git clone https://github.com/shinhancard/flux-oci-sample.git
cd flux-oci-sample
```

### Build the application

```bash
./gradlew build
```

### Run the application locally

```bash
./gradlew bootRun
```

The application will be available at http://localhost:8080

### Build the OCI image locally

```bash
./gradlew jib
```

## CI/CD Pipeline

This project includes a GitHub Actions workflow that automatically builds and pushes the OCI image to GitHub Container Registry whenever changes are pushed to the main branch.

The workflow is defined in `.github/workflows/build-and-push.yml` and performs the following steps:

1. Checks out the code
2. Sets up JDK 21
3. Builds the application
4. Logs in to GitHub Container Registry
5. Builds and pushes the OCI image using Jib

## Kubernetes Deployment with Flux and Kustomize

### How it works with Flux CD

[Flux CD](https://fluxcd.io/) is a GitOps operator for Kubernetes that ensures the state of your cluster matches the configuration stored in Git. This project is designed to work with Flux's OCI repository feature, which allows Flux to pull container images from OCI registries like GHCR.

### Kustomization Configuration

To use this image in your Kubernetes environment with Flux and Kustomize, create a Kustomization file that references the OCI image:

```yaml
# kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
- deployment.yaml
- service.yaml

images:
- name: ghcr.io/shinhancard/flux-oci-sample
  newTag: latest  # or use a specific version
```

### Deployment Example

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flux-oci-sample
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
        image: ghcr.io/shinhancard/flux-oci-sample:latest
        ports:
        - containerPort: 8080
```

### Service Example

```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: flux-oci-sample
spec:
  selector:
    app: flux-oci-sample
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP
```

### Flux Configuration

To configure Flux to watch for new versions of the OCI image and automatically update your cluster, create a Flux ImageRepository and ImagePolicy:

```yaml
# flux-system/imagerepository.yaml
apiVersion: image.toolkit.fluxcd.io/v1beta2
kind: ImageRepository
metadata:
  name: flux-oci-sample
  namespace: flux-system
spec:
  image: ghcr.io/shinhancard/flux-oci-sample
  interval: 1m0s
```

```yaml
# flux-system/imagepolicy.yaml
apiVersion: image.toolkit.fluxcd.io/v1beta2
kind: ImagePolicy
metadata:
  name: flux-oci-sample
  namespace: flux-system
spec:
  imageRepositoryRef:
    name: flux-oci-sample
  policy:
    semver:
      range: '>=0.0.0'
```

Then create a Kustomization resource to apply your manifests:

```yaml
# flux-system/kustomization.yaml
apiVersion: kustomize.toolkit.fluxcd.io/v1
kind: Kustomization
metadata:
  name: flux-oci-sample
  namespace: flux-system
spec:
  interval: 10m0s
  path: ./kubernetes
  prune: true
  sourceRef:
    kind: GitRepository
    name: flux-oci-sample
  targetNamespace: default
  images:
  - name: ghcr.io/shinhancard/flux-oci-sample
    newName: ghcr.io/shinhancard/flux-oci-sample
    policy:
      imageRepositoryRef:
        name: flux-oci-sample
      imagePolicy:
        name: flux-oci-sample
```

## Triggering Updates in Kubernetes

When a new version of the application is pushed to the main branch:

1. The GitHub Actions workflow builds a new OCI image and pushes it to GHCR
2. Flux detects the new image in GHCR based on the ImageRepository configuration
3. Flux updates the deployment in the cluster according to the ImagePolicy
4. Kubernetes pulls the new image and updates the running pods

This creates a fully automated CI/CD pipeline from code commit to deployment in your Kubernetes cluster.

## License

This project is licensed under the terms of the license included in the repository.