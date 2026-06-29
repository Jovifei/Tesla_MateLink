import SwiftUI
import SceneKit

// T-101: iOS SceneKit + .usdz model loading
// T-103: Vehicle state animation triggers
// Place tesla_model3.usdz in the app target's Bundle resources (Xcode > Target > Build Phases > Copy Bundle Resources)

struct Vehicle3DView: UIViewRepresentable {
    let carStatus: CarStatus?

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    func makeUIView(context: Context) -> SCNView {
        let sceneView = SCNView()
        sceneView.allowsCameraControl = true
        sceneView.autoenablesDefaultLighting = true
        sceneView.backgroundColor = .clear
        sceneView.antialiasingMode = .multisampling4X

        if let url = Bundle.main.url(forResource: "tesla_model3", withExtension: "usdz"),
           let scene = try? SCNScene(url: url, options: nil) {
            sceneView.scene = scene
            context.coordinator.rootNode = scene.rootNode
            context.coordinator.cacheNodes()
        } else {
            // Fallback placeholder: wireframe box when .usdz not bundled yet
            let placeholder = SCNScene()
            let box = SCNBox(width: 4.5, height: 1.4, length: 2.1, chamferRadius: 0.3)
            box.firstMaterial?.diffuse.contents = UIColor.systemBlue.withAlphaComponent(0.3)
            let node = SCNNode(geometry: box)
            placeholder.rootNode.addChildNode(node)
            sceneView.scene = placeholder
            context.coordinator.rootNode = placeholder.rootNode
        }
        return sceneView
    }

    func updateUIView(_ uiView: SCNView, context: Context) {
        guard let status = carStatus, let rootNode = context.coordinator.rootNode else { return }
        let coordinator = context.coordinator

        // Use cached node references instead of traversing scene graph every update
        coordinator.animateNode(coordinator.chargePortNode, visible: status.chargePortDoorOpen, duration: 0.6)

        for door in coordinator.doorNodes {
            if status.locked {
                door.opacity = 0.4
            } else {
                coordinator.animateOpacity(door, to: 1.0, duration: 0.4)
            }
        }

        if status.sentryMode {
            coordinator.startSentryPulse(rootNode)
        } else {
            coordinator.stopSentryPulse()
        }
    }

    final class Coordinator {
        var rootNode: SCNNode?
        var chargePortNode: SCNNode?
        var doorNodes: [SCNNode] = []
        private var sentryAction: SCNAction?

        /// Call after setting rootNode to cache node references
        func cacheNodes() {
            guard let root = rootNode else { return }
            chargePortNode = root.childNode(withName: "charge_port", recursively: true)
            doorNodes = root.childNodes(passingTest: { node, _ in
                node.name?.hasPrefix("door") == true
            })
        }

        func animateNode(_ node: SCNNode?, visible: Bool, duration: TimeInterval) {
            guard let node = node else { return }
            SCNTransaction.begin()
            SCNTransaction.animationDuration = duration
            node.scale = visible ? SCNVector3(1, 1, 1) : SCNVector3(0.01, 0.01, 0.01)
            node.opacity = visible ? 1.0 : 0.0
            SCNTransaction.commit()
        }

        func animateOpacity(_ node: SCNNode, to opacity: CGFloat, duration: TimeInterval) {
            SCNTransaction.begin()
            SCNTransaction.animationDuration = duration
            node.opacity = opacity
            SCNTransaction.commit()
        }

        func startSentryPulse(_ root: SCNNode) {
            guard sentryAction == nil else { return }
            let glow = SCNAction.customAction(duration: 1.5) { node, elapsed in
                let t = CGFloat(elapsed / 1.5)
                let brightness = 0.3 + 0.7 * abs(sin(t * .pi))
                node.childNodes.forEach { child in
                    child.geometry?.firstMaterial?.emission.contents =
                        UIColor.systemRed.withAlphaComponent(brightness * 0.15)
                }
            }
            let loop = SCNAction.repeatForever(glow)
            root.runAction(loop, forKey: "sentryPulse")
            sentryAction = loop
        }

        func stopSentryPulse() {
            guard sentryAction != nil else { return }
            rootNode?.removeAction(forKey: "sentryPulse")
            // Recursively clear emission on all nested nodes, not just direct children
            func clearEmission(on node: SCNNode) {
                node.geometry?.firstMaterial?.emission.contents = nil
                for child in node.childNodes {
                    clearEmission(on: child)
                }
            }
            if let root = rootNode {
                clearEmission(on: root)
            }
            sentryAction = nil
        }

        deinit {
            stopSentryPulse()
        }
    }
}
