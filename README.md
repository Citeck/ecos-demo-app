![Citeck ECOS Logo](https://raw.githubusercontent.com/Citeck/ecos-ui/develop/public/img/logo/ecos-logo.png)

# `ecos-demo-app`

Welcome to the Citeck `ecos-demo-app` repository! This repository contains demo application that demonstrates ECOS features.

When you start this application in default left menu will appear **'Demo type'** section with two journals:
1. **Demo type entities** - main demo journal with BPMN process and different demo actions. The demo scenario written below is based on this type. 
2. **Demo in-memory type** - journal with in-memory entities to demonstrate working with custom RecordsDAO defined in *ru.citeck.ecos.webapp.demo.records.DemoInMemRecordsDao*. You can create/view/edit/delete records in this journal and see what changed in *DemoInMemRecordsDao*.

## Get started

If you are new to ECOS platform and would like to run the software locally, we recommend you download the Dockerized version from [Demo repository](https://github.com/Citeck/ecos-community-demo).

## Demo scenario

1. Run **ecos-demo-app**.
2. Tap **'Create'** button in top-left corner in ECOS.
3. Chose **Demo type -> Demo type**.
4. Enter in field **'Text data'** value *'error'* and click **'Save'** button. You should see error from transactional listener defined in *ru.citeck.ecos.webapp.demo.events.DemoEcosEventListener*.
5. Change value of the field **'Text data'** to anything else and fill in other fields.
6. After creation you'll see information about created record:
    - Status will be **'New'**. This is defined in property **defaultStatus** in type config - *src/main/resources/eapps/artifacts/model/type/demo-type.yml*
    - Task widgets will show active task for current user. BPMN process started because we have process definition in *src/main/resources/eapps/artifacts/process/bpmn/demo-process.bpmn.xml* with flags *ecos:enabled="true"* and *ecos:autoStartEnabled="true"*.
7. Click the **'Done'** button in the current task widget.
8. Task will disappear and external task will be started - *ru.citeck.ecos.webapp.demo.exttask.DemoExternalTask*.
9. After ~5-10 seconds you can update your browser tab and see the new status **'Completed'** and filled field **'Field generated in external task'**. BPMN Process completed at this point.
10. You can click **'Send demo email'** to test custom action with email sending. 
    - Action class: *ru.citeck.ecos.webapp.demo.actions.SendDemoEmailAction*
    - Action definition: *src/main/resources/eapps/artifacts/ui/action/send-demo-email-action.yml*
    - Email template: *src/main/resources/eapps/artifacts/notification/template/demo-email.html.ftl*
    - Result email can be found in mailhog (if you don't change default email settings) - http://localhost:8025/
11. After email action testing you can call **'Create child entity'** to test ability to create linked entities by action.
    - Action definition: *src/main/resources/eapps/artifacts/ui/action/create-child-entity-action.yml*

## Useful Links

- [Documentation](https://citeck-ecos.readthedocs.io/ru/latest/index.html) provides more in-depth information.

## Dependencies

To run this application the following applications from ECOS deployment are needed:

* zookeeper
* rabbitmq
* ecos-model
* ecos-registry

## Development

To start your application in the dev profile, simply run:

```
./mvnw spring-boot:run
```

If your IDE supports starting Spring Boot applications directly, then you can easily run the class *'ru.citeck.ecos.webapp.demo.EcosDemoApp'* without additional setup.

### Building for production

To build the application for production run:

```
./mvnw -Pprod clean package jib:dockerBuild -Djib.docker.image.tag=custom 
```

To ensure everything worked, stop original ecos-demo-app container and start ecos-demo-app:custom instead of it.

### Testing

To launch your application's tests, run:

```
./mvnw clean test
```

## Contributing

We welcome contributions from the community to make ECOS even better. Everyone interacting in the Citeck project’s codebases, issue trackers, chat rooms, and forum is expected to follow the [contributor code of conduct](https://github.com/rubygems/rubygems/blob/master/CODE_OF_CONDUCT.md).

## Support

If you need any assistance or have any questions regarding Citeck `ecos-demo-app`, please create an issue in this repository or reach out to our [support team](mailto:support@citeck.ru).

## License

Citeck `ecos-demo-app` is released under the [GNU Lesser General Public License](LICENSE).
