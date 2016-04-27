package com.express.serviceImpl;

import com.express.daoImpl.*;
import com.express.model.*;
import com.express.serviceInterface.IDomainService;
import org.hibernate.criterion.Restrictions;
import utils.Utils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by violet on 2016/4/6.
 */
public class DomainService implements IDomainService {

    private RegionDao regionDao;
    private CityDao cityDao;
    private ProvinceDao provinceDao;
    private PackAndpackDao packAndpackDao;
    private ExpressAndPackageDao expressAndPackageDao;
    private PackageDao packageDao;
    private ExpressDao expressDao;
    private EmployeesDao employeesDao;
    private AddressDao addressDao;
    private CustomerDao customerDao;


    public RegionDao getRegionDao() {
        return regionDao;
    }

    public void setRegionDao(RegionDao regionDao) {
        this.regionDao = regionDao;
    }

    public CityDao getCityDao() {
        return cityDao;
    }

    public void setCityDao(CityDao cityDao) {
        this.cityDao = cityDao;
    }

    public ProvinceDao getProvinceDao() {
        return provinceDao;
    }

    public void setProvinceDao(ProvinceDao provinceDao) {
        this.provinceDao = provinceDao;
    }

    public PackAndpackDao getPackAndpackDao() {
        return packAndpackDao;
    }

    public void setPackAndpackDao(PackAndpackDao packAndpackDao) {
        this.packAndpackDao = packAndpackDao;
    }

    public ExpressAndPackageDao getExpressAndPackageDao() {
        return expressAndPackageDao;
    }

    public void setExpressAndPackageDao(ExpressAndPackageDao expressAndPackageDao) {
        this.expressAndPackageDao = expressAndPackageDao;
    }

    public PackageDao getPackageDao() {
        return packageDao;
    }

    public void setPackageDao(PackageDao packageDao) {
        this.packageDao = packageDao;
    }

    public CustomerDao getCustomerDao() {
        return customerDao;
    }

    public void setCustomerDao(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public AddressDao getAddressDao() {
        return addressDao;
    }

    public void setAddressDao(AddressDao addressDao) {
        this.addressDao = addressDao;
    }

    public EmployeesDao getEmployeesDao() {
        return employeesDao;
    }

    public void setEmployeesDao(EmployeesDao employeesDao) {
        this.employeesDao = employeesDao;
    }

    public ExpressDao getExpressDao() {
        return expressDao;
    }

    public void setExpressDao(ExpressDao expressDao) {
        this.expressDao = expressDao;
    }

    /////////////////////////////公共的接口（用户和工作人员都要用的）////////////////////////////

    @Override
    public String PrepareSendExpress(Integer sendAddressId, Integer recvAddressId) {
        ExpressEntity expressEntity = new ExpressEntity();

        //获取id 校验是否存在
        String packageId = Utils.getUUid();
        while (true) {
            List<PackageEntity> by = packageDao.findBy("id", true, Restrictions.eq("id", packageId));
            if (by.size() == 0)
                break;
            else
                packageId = Utils.getUUid();
        }

        //填充实体信息
        expressEntity.setSendAddressId(sendAddressId);
        expressEntity.setSendAddressId(recvAddressId);

        expressDao.save(expressEntity);

        //返回实体对象
        return expressEntity.getId();
    }

    @Override
    public PackageEntity CreateAPackage(Integer id) {
        PackageEntity packageEntity = new PackageEntity();

        //获取id 校验是否存在
        String packageId = Utils.getUUid();
        while (true) {
            List<PackageEntity> by = packageDao.findBy("id", true, Restrictions.eq("id", packageId));
            if (by.size() == 0)
                break;
            else
                packageId = Utils.getUUid();
        }

        //填充实体信息
        packageEntity.setTime(new Date());
        packageEntity.setId(packageId);
        packageEntity.setEmployeesId(id);

        packageDao.save(packageEntity);

        //返回实体对象
        return packageEntity;
    }

    @Override
    public Response LoadIntoPackage(String PackageId, String Id, Integer isPackage) {
        //如果不是包裹
        try {
            ExpressandpackageEntity expressandpackageEntity = new ExpressandpackageEntity(PackageId, Id, new Date());
            expressAndPackageDao.save(expressandpackageEntity);
            PackandpackEntity packandpackEntity = new PackandpackEntity();
            if (isPackage == 1) {
                //如果是包裹还需要存储父包裹表
                packAndpackDao.save(packandpackEntity);
            }
            return Response.ok(packandpackEntity).header("PackandpackClass", "R_PackandpackInfo").build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @Override
    public List<ExpressEntity> searchExpressInPackageById(String PackageId) {
        List<ExpressEntity> lists = new ArrayList<>();
        //按照条件查找相应的包裹里所有快件和包裹
        List<ExpressandpackageEntity> all = expressAndPackageDao.findBy("PackageId", true, Restrictions.eq("PackageId", PackageId));
        for (int i = 0; i < all.size(); ++i) {
            String expressId = all.get(i).getExpressId();
            //如果是快递则存入list中
            if (packageDao.get(expressId) == null)
                lists.add(expressDao.get(expressId));
        }
        return lists;
    }

    @Override
    public ExpressInfo getExpressInfoById(String id) {
        //返回快递对象实体
        ExpressInfo expressInfo = new ExpressInfo();
        //获取快递信息
        ExpressEntity expressEntity = expressDao.get(id);
        //获取发件人信息
        CustomerEntity customerEntity = customerDao.get(expressEntity.getCustomerId());
        //发件人地址
        AddressEntity sendAddressEntity = addressDao.get(expressEntity.getSendAddressId());
        //收件人地址
        AddressEntity recvAddressEntity = addressDao.get(expressEntity.getAccAddressId());
        //收件人regin
        RegionEntity sendRegionEntity = regionDao.get(sendAddressEntity.getRegionId());
        CityEntity sendCityEntity = cityDao.get(sendRegionEntity.getCityId());
        ProvinceEntity sendProvinceEntity = provinceDao.get(sendCityEntity.getPid());
        //发件人regin
        RegionEntity recvRegionEntity = regionDao.get(sendAddressEntity.getRegionId());
        CityEntity recvCityEntity = cityDao.get(recvRegionEntity.getCityId());
        ProvinceEntity recvProvinceEntity = provinceDao.get(recvCityEntity.getPid());

        //塞数据。。。我日好长、、、
        expressInfo.setID(expressEntity.getId());
        expressInfo.setSname(sendAddressEntity.getName());
        expressInfo.setStel(sendAddressEntity.getTelephone());
        expressInfo.setSadd(sendProvinceEntity.getPname() + "-" + sendCityEntity.getCname() + "-" + sendRegionEntity.getArea());
        expressInfo.setSaddinfo(sendAddressEntity.getAddress());
        expressInfo.setRname(recvAddressEntity.getName());
        expressInfo.setRtel(recvAddressEntity.getTelephone());
        expressInfo.setRadd(recvProvinceEntity.getPname() + "-" + recvCityEntity.getCname() + "-" + recvRegionEntity.getArea());
        expressInfo.setRaddinfo(recvAddressEntity.getAddress());
        expressInfo.setGetTime(expressEntity.getGetTime().toString());
        expressInfo.setOutTime(expressEntity.getOutTime().toString());
        expressInfo.setWeight(expressEntity.getWeight());
        expressInfo.setTranFee(expressEntity.getTranFee());
        expressInfo.setInsuFee(expressEntity.getInsuFee());

        expressInfo.setAcc1(expressEntity.getAcc1());
        expressInfo.setAcc2(expressEntity.getAcc2());

        return expressInfo;
    }

    @Override
    public List<ExpressInfo> getExpressInfoByTel(String tel) {
        List<ExpressInfo> lists = new ArrayList<>();
        //用户信息
        List<CustomerEntity> customer = customerDao.findBy("Telephone", true, Restrictions.eq("Telephone", tel));
        //手机号相关的快递
        List<ExpressEntity> by = expressDao.findBy("CustomerID", true, Restrictions.eq("CustomerID", customer.get(0).getId()));

        for(int i = 0; i < by.size(); ++i){
            ExpressInfo expressInfo = getExpressInfoById(by.get(i).getId());
            lists.add(expressInfo);
        }
        return lists;
    }

    @Override
    public PackageEntity findAPackageById(String PackageId) {
        //直接返回包裹对象实体
        return packageDao.get(PackageId);
    }

    @Override
    public List<PackageEntity> searchPackageInPackageById(String PackageId) {
        List<PackageEntity> lists = new ArrayList<>();
        //按照条件查找相应的包裹里所有快件和包裹
        List<ExpressandpackageEntity> all = expressAndPackageDao.findBy("PackageId", true, Restrictions.eq("PackageId", PackageId));
        for (int i = 0; i < all.size(); ++i) {
            String expressId = all.get(i).getExpressId();
            //如果不是快递则存入list中
            if (packageDao.get(expressId) != null)
                lists.add(packageDao.get(expressId));
        }
        return lists;
    }

    @Override
    public Response saveExpress(ExpressEntity obj) {
        try {
            //保存快递并返回状态
            expressDao.save(obj);
            return Response.ok(obj).header("ExpressClass", "R_ExpressInfo").build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @Override
    public List<ExpressEntity> getWork(Integer employeeId, Date starttime, Integer days) {
        //获取时间段
        Date endtime = (Date) starttime.clone();
        endtime.setTime(endtime.getTime() + days * 86400);

        List<ExpressEntity> lists = new ArrayList<>();
        //查找符合条件的包裹
        List<PackageEntity> by = packageDao.findBy("ID", true, Restrictions.eq("EmployeesID", employeeId), Restrictions.between("time", starttime, endtime));

        //递归查找包裹里的快递并存放到lists中
        for (int i = 0; i < by.size(); ++i) {
            findAllWork(lists, by.get(i).getId());
        }
        return lists;
    }

    //递归方法
    public void findAllWork(List<ExpressEntity> lists, String PackageId) {
        //获得包裹中的快递和包裹
        List<ExpressEntity> expressEntities = searchExpressInPackageById(PackageId);
        List<PackageEntity> packageEntities = searchPackageInPackageById(PackageId);
        if (expressEntities != null) {
            //快递直接加入工作量lists
            lists.addAll(expressEntities);
        }
        if (packageEntities != null) {
            //如果存在包裹则遍历包裹再进行一遍查询包裹中包裹和快递的操作
            for (int i = 0; i < packageEntities.size(); ++i)
                findAllWork(lists, packageEntities.get(i).getId());
        }
    }


    /////////////////////////////用户的接口////////////////////////////

    //通过用户id获得用户信息
    @Override
    public CustomerEntity getCustomerInfoById(int id) {
        return customerDao.get(id);
    }

    //通过用户手机号获得用户信息
    @Override
    public CustomerEntity getCustomerInfoByTel(String tel) {
        List<CustomerEntity> list = customerDao.getByTel(tel);
        if (list.size()!=0){
            return list.get(0);
        }
        return null;
    }

    //注册，并在注册过程中检查手机号是否注册过
    @Override
    public String registerByCus(CustomerEntity obj) {
        if (obj.getName()==null || obj.getTelephone()==null || obj.getPassword()==null)
            return "{\"registerstate\":\"null\"}";
        List<CustomerEntity> list = customerDao.getByTel(obj.getTelephone());
        if (list.size() == 0) {
            try {
                customerDao.save(obj);
                return "{\"registerstate\":\"true\"}";
            } catch (Exception e) {
                e.printStackTrace();
                return "{\"registerstate\":\"false\"}";
            }
        } else {
            return "{\"registerstate\":\"deny\"}";
        }
    }

    //更新用户信息
    @Override
    public String updateCustomerInfo(CustomerEntity obj) {
        try {
            customerDao.save(obj);
            return "{\"updateCustomerInfo\":\"true\"}";
        } catch (Exception e) {
            return "{\"updateCustomerInfo\":\"false\"}";
        }
    }

    //根据用户id删除用户信息
    @Override
    public Response deleteCustomerInfo(int id) {
        customerDao.removeById(id);
        return Response.ok("Deleted").header("EntityClass", "D_CustomerInfo").build();
    }

    //用户登陆post方法
    @Override
    public String login(CustomerEntity obj) {
        if (obj.getTelephone()==null || obj.getPassword()==null)
            return "{\"loginstate\":\"null\"}";
        List<CustomerEntity> list = customerDao.getByTel(obj.getTelephone());
        if (list.size() != 0) {
            CustomerEntity customerEntity = list.get(0);
            if (customerEntity.getPassword().equals(obj.getPassword())) {
                return "{\"name\":\"" + customerEntity.getName() + "\", \"loginstate\":\"true\"}";
            }
        }
        return "{\"loginstate\":\"false\"}";
    }

    //注销登陆
    @Override
    public void doLogOut(int cid) {

    }

    @Override
    public Response createExpress(String id, int cid) {
        return null;
    }

    /////////////////////////////工作人员的公共接口////////////////////////////

    //通过工作人员id查找工作人员信息
    @Override
    public EmployeesEntity getEmployeeInfoById(int id) {
        return employeesDao.get(id);
    }

    //更新或者是插入一条数据
    @Override
    public Response saveEmployeeInfo(EmployeesEntity obj) {
        try {
            employeesDao.save(obj);
            return Response.ok(obj).header("EntityClass", "R_EmployeeInfo").build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    //删除员工信息
    @Override
    public Response deleteEmployee(int id) {
        employeesDao.removeById(id);
        return Response.ok("Deleted").header("EntityClass", "D_Employee").build();
    }

    //员工通过手机号和密码登陆
    @Override
    public boolean doLoginByEmployee(String tel, String pwd) {
        List<EmployeesEntity> list = employeesDao.getByTel(tel);
        EmployeesEntity employeesEntity = new EmployeesEntity();
        if (list.size() != 0) {
            employeesEntity = list.get(0);
            if (employeesEntity.getPassword().equals(pwd)) {
                return true;
            }
        }
        return false;
    }

    //员工注销登陆
    @Override
    public void doLogOutByEmployee(int id) {

    }

    @Override
    public Response savePackage(PackageEntity obj) {
        return null;
    }

    @Override
    public ExpressEntity getPackageById(int pid) {
        return null;
    }



    /////////////////////////////快递员的接口////////////////////////////


    /////////////////////////////分拣员的接口////////////////////////////


}
