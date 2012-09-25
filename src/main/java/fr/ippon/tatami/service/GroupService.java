package fr.ippon.tatami.service;

import fr.ippon.tatami.domain.Group;
import fr.ippon.tatami.domain.User;
import fr.ippon.tatami.repository.*;
import fr.ippon.tatami.security.AuthenticationService;
import fr.ippon.tatami.service.util.DomainUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Service bean for managing groups.
 */
@Service
public class GroupService {

    private final Log log = LogFactory.getLog(GroupService.class);

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupMembersRepository groupMembersRepository;

    @Inject
    private GroupCounterRepository groupCounterRepository;

    @Inject
    private UserGroupsRepository userGroupsRepository;

    @Inject
    private UserRepository userRepository;

    public void createGroup(String groupName) {
        if (log.isDebugEnabled()) {
            log.debug("Creating group : " + groupName);
        }
        User currentUser = authenticationService.getCurrentUser();
        String domain = DomainUtil.getDomainFromLogin(currentUser.getLogin());
        String groupId = groupRepository.createGroup(domain, groupName);
        groupMembersRepository.addAdmin(groupId, currentUser.getLogin());
        groupCounterRepository.incrementGroupCounter(domain, groupId);
        userGroupsRepository.addGroupAsAdmin(currentUser.getLogin(), groupId);
    }

    public Collection<User> getMembersForGroup(String groupId) {
        Collection<String> logins = groupMembersRepository.findMembers(groupId);
        Collection<User> users = new ArrayList<User>();
        for (String memberLogin : logins) {
            User friend = userRepository.findUserByLogin(memberLogin);
            users.add(friend);
        }
        return users;
    }

    public Collection<Group> getGroupsForCurrentUser() {
        User currentUser = authenticationService.getCurrentUser();
        String domain = DomainUtil.getDomainFromLogin(currentUser.getLogin());
        Collection<String> groupIds = userGroupsRepository.findGroups(currentUser.getLogin());
        Collection<Group> groups = new TreeSet<Group>();
        for (String groupId : groupIds) {
            Group group = groupRepository.getGroupById(domain, groupId);
            long counter = groupCounterRepository.getGroupCounter(domain, groupId);
            group.setCounter(counter);
            groups.add(group);
        }
        return groups;
    }
}