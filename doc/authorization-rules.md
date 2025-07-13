# ShopX Authorization Rules Documentation

## Technical Documentation

### Role Hierarchy and Permissions

ShopX implements a role-based access control (RBAC) system with a hierarchical role structure. Each user can have different roles for different stores.

#### 1. Store Roles

| Role | Description | Inheritance |
|------|-------------|-------------|
| OWNER | Store owner with complete control | Inherits all permissions from ADMIN, MANAGER, and STAFF |
| ADMIN | Administrator with extensive management capabilities | Inherits all permissions from MANAGER and STAFF |
| MANAGER | Manager with operational control | Inherits all permissions from STAFF |
| STAFF | Basic staff member with limited access | Base role |

#### 2. Permission Mapping

| Resource | Action | Required Role |
|----------|--------|---------------|
| **Store** |
| View store details | VIEW | STAFF or higher |
| Edit store details | EDIT | MANAGER or higher |
| Delete store | DELETE | OWNER only |
| **Products** |
| View products | VIEW | STAFF or higher |
| Add/Edit products | EDIT | STAFF or higher |
| Delete products | DELETE | MANAGER or higher |
| **Categories** |
| View categories | VIEW | STAFF or higher |
| Add/Edit categories | EDIT | STAFF or higher |
| Delete categories | DELETE | MANAGER or higher |
| **Staff Management** |
| View staff members | VIEW | STAFF or higher |
| Invite staff members | CREATE | ADMIN or higher |
| Edit staff roles | EDIT | ADMIN or higher |
| Remove staff members | DELETE | ADMIN or higher |
| **Analytics** |
| View basic reports | VIEW | STAFF or higher |
| View financial data | VIEW_SENSITIVE | MANAGER or higher |
| Export reports | EXPORT | MANAGER or higher |
| **Marketing** |
| View campaigns | VIEW | STAFF or higher |
| Create/Edit campaigns | EDIT | MANAGER or higher |
| Approve campaign budget | APPROVE | ADMIN or higher |

#### 3. Technical Implementation

##### JWT Token Structure

```json
{
  "sub": "username",
  "userId": 123,
  "iat": 1723183711,
  "exp": 1723183819,
  "stores": [
    {
      "storeId": 1,
      "role": "OWNER"
    },
    {
      "storeId": 2,
      "role": "MANAGER"
    }
  ],
  "permissions": [
    "store:1:full_access",
    "store:2:edit"
  ]
}
```

##### Authorization Decision Flow

1. Extract user identity from Authentication context
2. Determine required permission for the requested resource and action
3. Check if the user has the required role for the resource's store
4. Apply any additional resource-specific authorization rules
5. Allow or deny access based on the result

##### Integration with Spring Security

The authorization system extends Spring Security through custom annotations and an aspect-oriented approach:

```java
@RequiresStoreRole(role = StoreRole.StoreRoleType.MANAGER)
public ResponseEntity<Product> updateProduct(Long productId) {
    // Method implementation
}
```

### API Endpoints and Required Authorization

| Endpoint | HTTP Method | Required Role | Notes |
|----------|-------------|--------------|-------|
| `/api/stores` | GET | Authenticated | Lists stores user has access to |
| `/api/stores` | POST | Authenticated | Any authenticated user can create a store |
| `/api/stores/{storeId}` | GET | STAFF | View specific store |
| `/api/stores/{storeId}` | PUT | MANAGER | Update store details |
| `/api/stores/{storeId}` | DELETE | OWNER | Delete store |
| `/api/stores/{storeId}/staff` | GET | STAFF | View store staff |
| `/api/stores/{storeId}/staff/{userId}/role` | PUT | ADMIN | Update staff role |
| `/api/stores/{storeId}/staff/{userId}` | DELETE | ADMIN | Remove staff member |
| `/api/invitations/{invitationId}/accept` | POST | Authenticated | User can only accept invitations sent to them |
| `/api/invitations/{invitationId}/reject` | POST | Authenticated | User can only reject invitations sent to them |
| `/api/users/profile` | PUT | Authenticated | Users can only edit their own profile |
| `/api/users/account` | DELETE | Authenticated | Users can only delete their own account |

---

## User Documentation: Understanding ShopX Permissions

### Introduction to ShopX Store Roles

ShopX uses a role-based system to determine what actions each team member can perform within your store. This ensures that your team members have access to the tools they need while maintaining appropriate security boundaries.

### Store Roles Explained

#### Store Owner
As a store owner, you have complete control over all aspects of your store.

**You can:**
- Manage all store settings and configurations
- Add, edit, and remove products and categories
- Create and manage marketing campaigns
- View all analytics and financial data
- Invite team members and assign roles
- Delete the store if needed

#### Store Admin
Admins have extensive management capabilities to help run the store.

**You can:**
- Manage store settings (except ownership transfer)
- Add, edit, and remove products and categories
- Create and manage marketing campaigns
- View all analytics and financial data
- Invite team members and assign roles

**You cannot:**
- Delete the store
- Transfer store ownership

#### Store Manager
Managers handle day-to-day operations and product management.

**You can:**
- Add, edit, and remove products and categories
- Create and manage marketing campaigns
- View analytics including financial data
- Manage basic store settings

**You cannot:**
- Delete the store
- Manage team members and roles
- Change critical store settings

#### Store Staff
Staff members handle basic store operations.

**You can:**
- Add and edit products and categories
- View basic analytics (non-financial)
- Process orders and customer service

**You cannot:**
- Delete products or categories
- Access financial data
- Manage store settings
- Invite or manage team members

### Managing Your Team

#### Inviting Team Members
1. Go to Settings > Team Members
2. Click "Invite Team Member"
3. Enter their email address and select a role
4. Click "Send Invitation"

#### Changing Team Member Roles
1. Go to Settings > Team Members
2. Find the team member in the list
3. Click "Edit Role" next to their name
4. Select the new role from the dropdown
5. Click "Save Changes"

#### Removing Team Members
1. Go to Settings > Team Members
2. Find the team member in the list
3. Click "Remove" next to their name
4. Confirm the removal

### Security Best Practices

- **Assign appropriate roles**: Give team members only the access they need to do their job
- **Review team access regularly**: Periodically review who has access to your store
- **Update roles as responsibilities change**: Adjust permissions when team members' duties change
- **Remove access promptly**: When team members leave, remove their access immediately

For additional security questions or role customization needs, please contact ShopX Support.