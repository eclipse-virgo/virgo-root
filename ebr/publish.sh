#!/usr/bin/env bash

SPRING_VERSION=4.3.27.RELEASE

for module in "test" aop aspects beans context context.support core expression jdbc jms messaging orm oxm transaction web webmvc webmvc.portlet websocket; do
  scp -r /tmp/ivy/org.eclipse.virgo.mirrored/org.springframework.${module}/${SPRING_VERSION} virgoBuild@build.eclipse.org:/shared/rt/virgo/ivy/bundles/release/org.eclipse.virgo.mirrored/org.springframework.${module}
done
